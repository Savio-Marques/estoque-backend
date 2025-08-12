package com.marques.estoque.service;

import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.Debtor;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.util.DebtorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@Slf4j
public class DebtorService {

    @Autowired
    private DebtorRepository debtorRepository;

    public DebtorDTO findById(Long id) {
        log.info("Buscando devedor por id para o usuário logado");
        return DebtorMapper.INSTANCE.toDTO(returnDebtorWithId(id, getCurrentUser()));
    }

    public DebtorDTO findByName(String name) {
        log.info("Buscando devedor por nome para o usuário logado");
        return DebtorMapper.INSTANCE.toDTO(returnDebtorWithName(name, getCurrentUser()));
    }

    public List<DebtorDTO> findAll() {
        log.info("Buscando todos os devedores para o usuário logado");
        return DebtorMapper.INSTANCE.toDTOList(debtorRepository.findAllByUser(getCurrentUser()));
    }

    public DebtorDTO save(DebtorDTO debtorDTO) {
        log.info("Criando um devedor para o usuário logado");
        validateDebtor(debtorDTO.getName(), debtorDTO.getValue(), debtorDTO.getId());

        Debtor debtor =  DebtorMapper.INSTANCE.toDebtor(debtorDTO);

        debtor.setDate(LocalDateTime.now(ZoneOffset.UTC));

        debtor.setUser(getCurrentUser());

        return DebtorMapper.INSTANCE.toDTO(debtorRepository.save(debtor));
    }

    public DebtorDTO update (Long id, DebtorDTO debtorDTO) {
        log.info("Atualizando um devedor para o usuário logado");
        validateDebtor(debtorDTO.getName(), debtorDTO.getValue(), debtorDTO.getId());

        Debtor debtor = returnDebtorWithId(id, getCurrentUser());

        updateDebtor(debtor, debtorDTO);

        return DebtorMapper.INSTANCE.toDTO(debtorRepository.save(debtor));
    }

    public String delete (Long id) {
        log.info("Deletando um devedor para o usuário logado");
        if (!debtorRepository.existsByIdAndUser(id, getCurrentUser())){
            log.error("Devedor não encontrado com o id: {}",id);
            throw new NotFoundException("Devedor com o id " + id + " não encontrado");
        }

        debtorRepository.deleteById(id);
        return "Devedor com id " + id + " deletado com sucesso";
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

    private void updateDebtor(Debtor debtor, DebtorDTO debtorDTO) {
        debtor.setName(debtorDTO.getName());
        debtor.setValue(debtorDTO.getValue());
        debtor.setDescription(debtorDTO.getDescription());
        debtor.setUser(getCurrentUser());
    }

    private void validateDebtor(String name, BigDecimal value , Long id) {
        if (name == null || name.isEmpty()) {
            log.error("O nome do devedor não pode ser nulo ou vazio");
            throw new ArgumentException("O nome do produto não pode ser vazio");
        }

        if (value == null) {
            log.error("O valor devido não pode ser nula");
            throw new ArgumentException("O valor devido não pode ser nulo");
        }

        if (id == null) {
            log.error("O Id da categoria não pode ser nulo");
            throw new ArgumentException("O Id da categoria não pode ser nulo");
        }
    }
}
