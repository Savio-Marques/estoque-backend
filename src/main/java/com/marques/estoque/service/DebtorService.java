package com.marques.estoque.service;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.dto.DebtorsPageDTO;
import com.marques.estoque.dto.SummaryDebtorsDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.repository.projection.DebtorSummaryProjection;
import com.marques.estoque.util.DebtorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class DebtorService {

    private final DebtorRepository debtorRepository;
    private final DebtorMapper debtorMapper;

    @Transactional(readOnly = true)
    public DebtorDTO findById(Long id) {
        log.info("Buscando devedor por id para o usuário logado");
        return debtorMapper.toDTO(returnDebtorWithId(id, getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public DebtorDTO findByName(String name) {
        log.info("Buscando devedor por nome para o usuário logado");
        return debtorMapper.toDTO(returnDebtorWithName(name, getCurrentUser()));
    }

    @Transactional(readOnly = true)
    public List<DebtorDTO> findAll() {
        log.info("Buscando todos os devedores para o usuário logado");
        return debtorMapper.toDTOList(debtorRepository.findAllByUser(getCurrentUser()));
    }

    @Transactional
    public DebtorDTO save(DebtorDTO debtorDTO) {
        log.info("Criando um devedor para o usuário logado");
        String name = normalizeName(debtorDTO.getName());
        validateDebtor(name, debtorDTO.getValue());

        Debtor debtor = debtorMapper.toDebtor(debtorDTO);
        debtor.setName(name);
        debtor.setDate(LocalDateTime.now(ZoneOffset.UTC));
        debtor.setUser(getCurrentUser());

        return debtorMapper.toDTO(debtorRepository.save(debtor));
    }

    @Transactional
    public DebtorDTO update (Long id, DebtorDTO debtorDTO) {
        log.info("Atualizando um devedor para o usuário logado");
        String name = normalizeName(debtorDTO.getName());

        validateDebtor(name, debtorDTO.getValue());

        Debtor debtor = returnDebtorWithId(id, getCurrentUser());

        debtor.setName(name);
        debtor.setValue(debtorDTO.getValue());
        debtor.setDescription(debtorDTO.getDescription());
        debtor.setUser(getCurrentUser());

        return debtorMapper.toDTO(debtorRepository.save(debtor));
    }

    @Transactional
    public void delete (Long id) {
        log.info("Deletando um devedor para o usuário logado");
        returnDebtorWithId(id, getCurrentUser());

        debtorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public SummaryDebtorsDTO summaryDebtors() {
        log.info("Processando sumário de devedores");

        DebtorSummaryProjection projection = debtorRepository.getDebtorSummary(getCurrentUser());

        return new SummaryDebtorsDTO(
                projection.getTotalValue(),
                projection.getTotalDebtors()
        );
    }

    /*

    -------------FUNÇÕES AUXILIARES-------------

    */

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private Debtor returnDebtorWithId(Long id, User user) {
        return debtorRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new NotFoundException("Devedores com o id: " + id + " não encontrado"));
    }

    private Debtor returnDebtorWithName(String name, User user) {
        return debtorRepository.findByNameIgnoreCaseAndUser(name, user)
                .orElseThrow(() -> new NotFoundException("Devedores com o id: " + name + " não encontrado"));
    }

    private String normalizeName(String name) {
        if (name == null ) return null;
        return name.trim();
    }

    private void validateDebtor(String name, BigDecimal value) {
        if (name == null || name.isBlank()) {
            log.error("O nome do devedor não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do produto não pode ser vazio");
        }

        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            log.error("O valor devido não pode ser nula ou menor que zero");
            throw new ArgumentException("O valor devido não pode ser nula ou menor que zero");
        }
    }
}
