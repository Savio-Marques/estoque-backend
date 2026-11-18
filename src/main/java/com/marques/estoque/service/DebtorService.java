package com.marques.estoque.service;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.dto.DebtorTransactionDTO;
import com.marques.estoque.dto.DebtorsPageDTO;
import com.marques.estoque.dto.SummaryDebtorsDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.DebtorTransaction;
import com.marques.estoque.model.DebtorTransactionType;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.repository.DebtorTransactionRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.util.DebtorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebtorService {

    private final DebtorRepository debtorRepository;
    private final DebtorTransactionRepository debtorTransactionRepository;
    private final DebtorMapper debtorMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ProductService productService;

    public DebtorDTO findById(Long id) {
        log.info("Buscando devedor por id para o usuário logado");
        return debtorMapper.toDTO(returnDebtorWithId(id, authenticatedUserProvider.getCurrentUser()));
    }

    public DebtorDTO findByName(String name) {
        log.info("Buscando devedor por nome para o usuário logado");
        return debtorMapper.toDTO(returnDebtorWithName(name, authenticatedUserProvider.getCurrentUser()));
    }

    public List<DebtorDTO> findAll() {
        log.info("Buscando todos os devedores em aberto para o usuário logado");
        return debtorMapper.toDTOList(debtorRepository.findAllByUserAndPaidFalse(authenticatedUserProvider.getCurrentUser()));
    }

    @Transactional
    public DebtorDTO save(DebtorDTO debtorDTO) {
        log.info("Criando ou reabrindo lançamento de devedor para o usuário logado");
        validateDebtor(debtorDTO.getName(), debtorDTO.getValue());

        User currentUser = authenticatedUserProvider.getCurrentUser();
        applyStockDeductionIfRequested(currentUser, debtorDTO);

        String trimmedName = debtorDTO.getName().trim();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        Optional<Debtor> settledMatch = debtorRepository
                .findFirstByUserAndPaidTrueAndNameIgnoreCaseOrderBySettledAtDescIdDesc(currentUser, trimmedName);

        Debtor debtor;
        if (settledMatch.isPresent()) {
            debtor = settledMatch.get();
            debtor.setName(trimmedName);
            debtor.setDescription(debtorDTO.getDescription());
            debtor.setValue(debtorDTO.getValue());
            debtor.setDate(now);
            debtor.setPaid(false);
            debtor.setSettledAt(null);
        } else {
            debtor = debtorMapper.toDebtor(debtorDTO);
            debtor.setName(trimmedName);
            debtor.setDate(now);
            debtor.setPaid(false);
            debtor.setSettledAt(null);
            debtor.setUser(currentUser);
        }

        Debtor saved = debtorRepository.save(debtor);
        recordTransaction(
                currentUser,
                saved,
                DebtorTransactionType.LANCAMENTO,
                saved.getValue(),
                BigDecimal.ZERO,
                saved.getValue(),
                saved.getDescription()
        );

        return debtorMapper.toDTO(saved);
    }

    @Transactional
    public DebtorDTO update(Long id, DebtorDTO debtorDTO) {
        log.info("Atualizando um devedor para o usuário logado");
        validateDebtor(debtorDTO.getName(), debtorDTO.getValue());

        User currentUser = authenticatedUserProvider.getCurrentUser();
        Debtor debtor = returnDebtorWithId(id, currentUser);
        applyStockDeductionIfRequested(currentUser, debtorDTO);

        BigDecimal previousBalance = debtor.getValue() != null ? debtor.getValue() : BigDecimal.ZERO;
        BigDecimal newBalance = debtorDTO.getValue();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        updateDebtor(debtor, debtorDTO, currentUser);

        if (newBalance.compareTo(BigDecimal.ZERO) > 0 && Boolean.TRUE.equals(debtor.getPaid())) {
            debtor.setPaid(false);
            debtor.setSettledAt(null);
            debtor.setDate(now);
        } else if (newBalance.compareTo(BigDecimal.ZERO) == 0 && previousBalance.compareTo(BigDecimal.ZERO) > 0) {
            debtor.setPaid(true);
            debtor.setSettledAt(now);
        }

        Debtor saved = debtorRepository.save(debtor);

        if (newBalance.compareTo(previousBalance) != 0) {
            BigDecimal delta = newBalance.subtract(previousBalance);
            DebtorTransactionType txType = newBalance.compareTo(BigDecimal.ZERO) == 0
                    ? DebtorTransactionType.QUITACAO
                    : (delta.compareTo(BigDecimal.ZERO) > 0
                            ? DebtorTransactionType.ACRESCIMO
                            : DebtorTransactionType.AJUSTE);

            String note = (debtorDTO.getTransactionNote() != null && !debtorDTO.getTransactionNote().trim().isEmpty())
                    ? debtorDTO.getTransactionNote().trim()
                    : saved.getDescription();

            recordTransaction(currentUser, saved, txType, delta, previousBalance, newBalance, note);
        }

        return debtorMapper.toDTO(saved);
    }

    @Transactional
    public String delete(Long id) {
        log.info("Deletando administrativamente um devedor para o usuário logado");
        User currentUser = authenticatedUserProvider.getCurrentUser();
        if (!debtorRepository.existsByIdAndUser(id, currentUser)) {
            log.error("Devedor não encontrado com o id: {}", id);
            throw new NotFoundException("Devedor com o id " + id + " não encontrado");
        }

        debtorTransactionRepository.deleteAllByDebtorIdAndUser(id, currentUser);
        debtorRepository.deleteById(id);
        return "Devedor com id " + id + " deletado com sucesso";
    }

    @Transactional
    public DebtorDTO registerPartialPayment(Long id, BigDecimal amount) {
        log.info("Registrando pagamento parcial para o devedor id: {}", id);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArgumentException("O valor do pagamento deve ser maior que zero.");
        }

        User currentUser = authenticatedUserProvider.getCurrentUser();
        Debtor debtor = returnDebtorWithId(id, currentUser);
        BigDecimal previousBalance = debtor.getValue() != null ? debtor.getValue() : BigDecimal.ZERO;

        if (amount.compareTo(previousBalance) > 0) {
            throw new ArgumentException("O valor abatido não pode ser maior que a dívida atual.");
        }

        BigDecimal remaining = previousBalance.subtract(amount);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            debtor.setValue(BigDecimal.ZERO);
            debtor.setPaid(true);
            debtor.setSettledAt(LocalDateTime.now(ZoneOffset.UTC));
            Debtor saved = debtorRepository.save(debtor);

            recordTransaction(
                    currentUser,
                    saved,
                    DebtorTransactionType.QUITACAO,
                    amount.negate(),
                    previousBalance,
                    BigDecimal.ZERO,
                    "Pagamento integral do saldo restante"
            );
            return null;
        }

        debtor.setValue(remaining);
        Debtor saved = debtorRepository.save(debtor);
        recordTransaction(
                currentUser,
                saved,
                DebtorTransactionType.ABATIMENTO,
                amount.negate(),
                previousBalance,
                remaining,
                "Abatimento parcial"
        );
        return debtorMapper.toDTO(saved);
    }

    @Transactional
    public DebtorDTO settle(Long id) {
        log.info("Quitando integralmente o devedor id: {}", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();
        Debtor debtor = returnDebtorWithId(id, currentUser);

        BigDecimal previousBalance = debtor.getValue() != null ? debtor.getValue() : BigDecimal.ZERO;
        if (Boolean.TRUE.equals(debtor.getPaid()) || previousBalance.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ArgumentException("Esta conta já se encontra quitada.");
        }

        debtor.setValue(BigDecimal.ZERO);
        debtor.setPaid(true);
        debtor.setSettledAt(LocalDateTime.now(ZoneOffset.UTC));
        Debtor saved = debtorRepository.save(debtor);

        recordTransaction(
                currentUser,
                saved,
                DebtorTransactionType.QUITACAO,
                previousBalance.negate(),
                previousBalance,
                BigDecimal.ZERO,
                "Quitação integral"
        );

        return debtorMapper.toDTO(saved);
    }

    public List<DebtorTransactionDTO> findTransactionsByDebtor(Long id) {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        returnDebtorWithId(id, currentUser);
        return debtorTransactionRepository.findAllByDebtorIdAndUserOrderByCreatedAtDescIdDesc(id, currentUser)
                .stream()
                .map(this::toTransactionDTO)
                .toList();
    }

    public List<DebtorTransactionDTO> findRecentTransactions() {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return debtorTransactionRepository.findTop50ByUserOrderByCreatedAtDescIdDesc(currentUser)
                .stream()
                .map(this::toTransactionDTO)
                .toList();
    }

    public SummaryDebtorsDTO summaryDebtors() {
        log.info("Contando quantidade e total de valores dos devedores em aberto");
        User currentUser = authenticatedUserProvider.getCurrentUser();

        BigDecimal totalValue = Optional.ofNullable(debtorRepository.sumTotalValueByUser(currentUser))
                .orElse(BigDecimal.ZERO);

        Integer totalDebtors = debtorRepository.countTotalDebtorsByUser(currentUser);

        return new SummaryDebtorsDTO(totalValue, totalDebtors);
    }

    public DebtorsPageDTO findDebtorsWithFilters(String clientName) {
        return findDebtorsWithFilters(clientName, false);
    }

    public DebtorsPageDTO findDebtorsWithFilters(String clientName, boolean paid) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        List<Debtor> filteredDebtors;
        boolean hasNameFilter = clientName != null && !clientName.trim().isEmpty();

        if (hasNameFilter) {
            filteredDebtors = debtorRepository.findByUserAndPaidAndNameContainingIgnoreCase(currentUser, paid, clientName.trim());
            if (paid) {
                filteredDebtors = filteredDebtors.stream()
                        .sorted(Comparator.comparing(
                                Debtor::getSettledAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ))
                        .toList();
            }
        } else if (paid) {
            filteredDebtors = debtorRepository.findAllByUserAndPaidTrueOrderBySettledAtDescIdDesc(currentUser);
        } else {
            filteredDebtors = debtorRepository.findAllByUserAndPaidFalse(currentUser);
        }

        Integer totalDebtors = filteredDebtors.size();
        BigDecimal totalValue = filteredDebtors.stream()
                .map(Debtor::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SummaryDebtorsDTO summary = new SummaryDebtorsDTO(totalValue, totalDebtors);
        List<DebtorDTO> debtorDTOs = debtorMapper.toDTOList(filteredDebtors);

        return new DebtorsPageDTO(debtorDTOs, summary);
    }

    private void applyStockDeductionIfRequested(User currentUser, DebtorDTO debtorDTO) {
        if (!Boolean.FALSE.equals(debtorDTO.getDeductFromStock())
                && debtorDTO.getStockItems() != null
                && !debtorDTO.getStockItems().isEmpty()) {
            productService.deductStockForDebtor(currentUser, debtorDTO.getStockItems());
        }
    }

    private void recordTransaction(
            User user,
            Debtor debtor,
            DebtorTransactionType type,
            BigDecimal amount,
            BigDecimal previousBalance,
            BigDecimal newBalance,
            String description
    ) {
        DebtorTransaction tx = new DebtorTransaction();
        tx.setUser(user);
        tx.setDebtorId(debtor.getId());
        tx.setDebtorName(debtor.getName());
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setPreviousBalance(previousBalance);
        tx.setNewBalance(newBalance);
        tx.setDescription(description != null && description.length() > 255 ? description.substring(0, 255) : description);
        tx.setCreatedAt(LocalDateTime.now(ZoneOffset.UTC));
        debtorTransactionRepository.save(tx);
    }

    private DebtorTransactionDTO toTransactionDTO(DebtorTransaction tx) {
        return new DebtorTransactionDTO(
                tx.getId(),
                tx.getDebtorId(),
                tx.getDebtorName(),
                tx.getTransactionType().name(),
                tx.getAmount(),
                tx.getPreviousBalance(),
                tx.getNewBalance(),
                tx.getDescription(),
                tx.getCreatedAt()
        );
    }

    private Debtor returnDebtorWithId(Long id, User user) {
        return debtorRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Devedores com o id: " + id + " não encontrado"));
    }

    private Debtor returnDebtorWithName(String name, User user) {
        return debtorRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new NotFoundException("Devedor com o nome: " + name + " não encontrado"));
    }

    private void updateDebtor(Debtor debtor, DebtorDTO debtorDTO, User currentUser) {
        debtor.setName(debtorDTO.getName().trim());
        debtor.setValue(debtorDTO.getValue());
        debtor.setDescription(debtorDTO.getDescription());
        debtor.setUser(currentUser);
    }

    private void validateDebtor(String name, BigDecimal value) {
        if (name == null || name.trim().isEmpty()) {
            log.error("O nome do devedor não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do devedor não pode ser vazio");
        }

        if (value == null) {
            log.error("O valor devido não pode ser nulo");
            throw new ArgumentException("O valor devido não pode ser nulo");
        }
    }
}
