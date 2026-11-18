package com.marques.estoque.service;

import com.marques.estoque.dto.DebtorStockItemDTO;
import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.dto.StockMovementDTO;
import com.marques.estoque.dto.SummaryProductDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.product.Product;
import com.marques.estoque.model.product.StockMovement;
import com.marques.estoque.model.product.StockMovementType;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.repository.StockMovementRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private static final int DEFAULT_LOW_STOCK_THRESHOLD = 5;

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final CategoryService categoryService;
    private final ProductMapper productMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public ProductDTO findById(Long id) {
        log.info("Buscando produto por id para o usuário logado");
        return productMapper.toDTO(returnProductWithId(id, authenticatedUserProvider.getCurrentUser()));
    }

    public List<ProductDTO> findByName(String name) {
        log.info("Buscando produto por nome para o usuário logado");
        return productMapper.toDTOList(
                productRepository.findByNameContainingIgnoreCaseAndUser(name, authenticatedUserProvider.getCurrentUser())
        );
    }

    @Transactional
    public ProductDTO save(ProductDTO productDTO) {
        log.info("Salvando produto no banco de dados para o usuário logado");
        User currentUser = authenticatedUserProvider.getCurrentUser();

        validateProduct(productDTO.getName(), productDTO.getQtd());

        if (productRepository.existsByNameIgnoreCaseAndUser(productDTO.getName(), currentUser)) {
            throw new DuplicateDataException("Já existe um produto cadastrado com esse nome.");
        }

        Product product = productMapper.toEntity(productDTO);
        applyDefaults(product, productDTO);
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), currentUser));
        product.setUser(currentUser);

        Product saved = productRepository.save(product);
        recordMovement(currentUser, saved.getName(), StockMovementType.CADASTRO, 0, saved.getQtd());

        return productMapper.toDTO(saved);
    }

    @Transactional
    public ProductDTO update(Long id, ProductDTO productDTO) {
        log.info("Atualizando id: {} no banco de dados", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Product product = returnProductWithId(id, currentUser);

        validateProduct(productDTO.getName(), productDTO.getQtd());

        if (productRepository.existsByNameIgnoreCaseAndUserAndIdNot(productDTO.getName(), currentUser, id)) {
            throw new DuplicateDataException("Já existe um produto cadastrado com esse nome.");
        }

        int previousQtd = product.getQtd();
        updateProductEntity(product, productDTO, currentUser);

        Product saved = productRepository.save(product);
        if (previousQtd != saved.getQtd()) {
            StockMovementType type = saved.getQtd() > previousQtd
                    ? StockMovementType.ENTRADA
                    : StockMovementType.SAIDA;
            recordMovement(currentUser, saved.getName(), type, previousQtd, saved.getQtd());
        }

        return productMapper.toDTO(saved);
    }

    @Transactional
    public String delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Product product = returnProductWithId(id, currentUser);

        recordMovement(currentUser, product.getName(), StockMovementType.EXCLUSAO, product.getQtd(), 0);
        productRepository.delete(product);
        return "Produto com id " + id + " deletado com sucesso";
    }

    @Transactional
    public void deductStockForDebtor(User currentUser, List<DebtorStockItemDTO> stockItems) {
        if (stockItems == null || stockItems.isEmpty()) {
            return;
        }

        Map<Long, Integer> consolidatedQuantities = new LinkedHashMap<>();
        for (DebtorStockItemDTO item : stockItems) {
            if (item == null || item.getProductId() == null) {
                throw new ArgumentException("O produto selecionado na baixa de estoque é inválido.");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new ArgumentException("A quantidade informada para baixa de estoque deve ser maior que zero.");
            }
            consolidatedQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        Map<Product, Integer> validatedProducts = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : consolidatedQuantities.entrySet()) {
            Product product = returnProductWithId(entry.getKey(), currentUser);
            int requestedQty = entry.getValue();
            if (product.getQtd() < requestedQty) {
                throw new ArgumentException(
                        "Estoque insuficiente para o produto \"" + product.getName()
                                + "\" (disponível: " + product.getQtd() + " un.)."
                );
            }
            validatedProducts.put(product, requestedQty);
        }

        for (Map.Entry<Product, Integer> entry : validatedProducts.entrySet()) {
            Product product = entry.getKey();
            int previousQtd = product.getQtd();
            int newQtd = previousQtd - entry.getValue();
            product.setQtd(newQtd);
            productRepository.save(product);
            recordMovement(currentUser, product.getName(), StockMovementType.SAIDA, previousQtd, newQtd);
        }
    }

    public SummaryProductDTO summaryProduct() {
        log.info("Contabilizando dados dos produtos");
        User currentUser = authenticatedUserProvider.getCurrentUser();

        Integer total = productRepository.countTotalProductsByUser(currentUser);
        Integer lowStock = productRepository.countLowStockProductsByUser(currentUser);
        Integer noStock = productRepository.countOutOfStockProductsByUser(currentUser);
        BigDecimal totalCost = productRepository.sumTotalStockCostValueByUser(currentUser);
        BigDecimal totalSale = productRepository.sumTotalStockSaleValueByUser(currentUser);

        return new SummaryProductDTO(
                total,
                lowStock,
                noStock,
                totalCost != null ? totalCost : BigDecimal.ZERO,
                totalSale != null ? totalSale : BigDecimal.ZERO
        );
    }

    public List<ProductDTO> findAll() {
        List<Product> productList = productRepository.findAllByUserOrderByIdAsc(authenticatedUserProvider.getCurrentUser());
        return productMapper.toDTOList(productList);
    }

    public List<ProductDTO> lowStockProductFilter() {
        List<Product> lowStockProducts = productRepository.findLowStockProductsOrderByIdAsc(
                authenticatedUserProvider.getCurrentUser()
        );
        return productMapper.toDTOList(lowStockProducts);
    }

    public List<ProductDTO> noStockProductFilter() {
        List<Product> noStockProducts = productRepository.findOutOfStockProductsOrderByIdAsc(
                authenticatedUserProvider.getCurrentUser()
        );
        return productMapper.toDTOList(noStockProducts);
    }

    public List<StockMovementDTO> findRecentMovements() {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return stockMovementRepository.findTop50ByUserOrderByCreatedAtDesc(currentUser).stream()
                .map(m -> new StockMovementDTO(
                        m.getId(),
                        m.getProductName(),
                        m.getMovementType().name(),
                        m.getPreviousQuantity(),
                        m.getNewQuantity(),
                        m.getQuantityDelta(),
                        m.getCreatedAt()
                ))
                .toList();
    }

    private Product returnProductWithId(Long id, User user) {
        return productRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Produto com o id: " + id + " não encontrado"));
    }

    private void updateProductEntity(Product product, ProductDTO productDTO, User currentUser) {
        product.setName(productDTO.getName());
        product.setQtd(productDTO.getQtd());
        applyDefaults(product, productDTO);
        product.setCategories(categoryService.returnCategory(productDTO.getCategoryId(), currentUser));
    }

    private void applyDefaults(Product product, ProductDTO dto) {
        product.setCostPrice(dto.getCostPrice() != null ? dto.getCostPrice() : BigDecimal.ZERO);
        product.setSalePrice(dto.getSalePrice() != null ? dto.getSalePrice() : BigDecimal.ZERO);
        product.setMinQuantity(dto.getMinQuantity() != null ? dto.getMinQuantity() : DEFAULT_LOW_STOCK_THRESHOLD);
    }

    private void recordMovement(User user, String productName, StockMovementType type, int previousQtd, int newQtd) {
        StockMovement movement = new StockMovement();
        movement.setUser(user);
        movement.setProductName(productName);
        movement.setMovementType(type);
        movement.setPreviousQuantity(previousQtd);
        movement.setNewQuantity(newQtd);
        movement.setQuantityDelta(newQtd - previousQtd);
        movement.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        stockMovementRepository.save(movement);
    }

    private void validateProduct(String name, Integer qtd) {
        if (name == null || name.trim().isEmpty()) {
            log.error("O nome do produto não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do produto não pode ser vazio");
        }

        if (qtd == null || qtd < 0) {
            log.error("A quantidade do produto não pode ser nula ou negativa");
            throw new ArgumentException("A quantidade do produto deve ser maior ou igual a zero");
        }
    }
}
