package com.marques.estoque.service;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.dto.DebtorsPageDTO;
import com.marques.estoque.dto.SummaryDebtorsDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.util.DebtorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DebtorService {

    private final DebtorRepository debtorRepository;
    private final DebtorMapper debtorMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public DebtorDTO findById(Long id) {
        log.info("Buscando devedor por id para o usuário logado");
        return debtorMapper.toDTO(returnDebtorWithId(id, authenticatedUserProvider.getCurrentUser()));
    }

    public DebtorDTO findByName(String name) {
        log.info("Buscando devedor por nome para o usuário logado");
        return debtorMapper.toDTO(returnDebtorWithName(name, authenticatedUserProvider.getCurrentUser()));
    }

    public List<DebtorDTO> findAll() {
        log.info("Buscando todos os devedores para o usuário logado");
        return debtorMapper.toDTOList(debtorRepository.findAllByUser(authenticatedUserProvider.getCurrentUser()));
    }

    public DebtorDTO save(DebtorDTO debtorDTO) {
        log.info("Criando um devedor para o usuário logado");
        validateDebtor(debtorDTO.getName(), debtorDTO.getValue());

        Debtor debtor = debtorMapper.toDebtor(debtorDTO);
        debtor.setDate(LocalDateTime.now(ZoneOffset.UTC));
        debtor.setUser(authenticatedUserProvider.getCurrentUser());

        return debtorMapper.toDTO(debtorRepository.save(debtor));
    }

    public DebtorDTO update(Long id, DebtorDTO debtorDTO) {
        log.info("Atualizando um devedor para o usuário logado");
        validateDebtor(debtorDTO.getName(), debtorDTO.getValue());

        User currentUser = authenticatedUserProvider.getCurrentUser();
        Debtor debtor = returnDebtorWithId(id, currentUser);

        updateDebtor(debtor, debtorDTO, currentUser);

        return debtorMapper.toDTO(debtorRepository.save(debtor));
    }

    public String delete(Long id) {
        log.info("Deletando um devedor para o usuário logado");
        if (!debtorRepository.existsByIdAndUser(id, authenticatedUserProvider.getCurrentUser())) {
            log.error("Devedor não encontrado com o id: {}", id);
            throw new NotFoundException("Devedor com o id " + id + " não encontrado");
        }

        debtorRepository.deleteById(id);
        return "Devedor com id " + id + " deletado com sucesso";
    }

    public SummaryDebtorsDTO summaryDebtors() {
        log.info("Contando quantidade e total de valores dos devedores");
        User currentUser = authenticatedUserProvider.getCurrentUser();

        BigDecimal totalValue = Optional.ofNullable(debtorRepository.sumTotalValueByUser(currentUser))
                .orElse(BigDecimal.ZERO);

        Integer totalDebtors = debtorRepository.countTotalDebtorsByUser(currentUser);

        return new SummaryDebtorsDTO(totalValue, totalDebtors);
    }

    public DebtorsPageDTO findDebtorsWithFilters(String clientName) {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        List<Debtor> filteredDebtors;

        boolean hasNameFilter = clientName != null && !clientName.trim().isEmpty();

        if (hasNameFilter) {
            filteredDebtors = debtorRepository.findByUserAndNameContainingIgnoreCase(currentUser, clientName);
        } else {
            filteredDebtors = debtorRepository.findAllByUser(currentUser);
        }

        Integer totalDebtors = filteredDebtors.size();
        BigDecimal totalValue = filteredDebtors.stream()
                .map(Debtor::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        SummaryDebtorsDTO summary = new SummaryDebtorsDTO(totalValue, totalDebtors);
        List<DebtorDTO> debtorDTOs = debtorMapper.toDTOList(filteredDebtors);

        return new DebtorsPageDTO(debtorDTOs, summary);
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
        debtor.setName(debtorDTO.getName());
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
