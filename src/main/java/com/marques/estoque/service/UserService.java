package com.marques.estoque.service;

import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.dto.UserResponseDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.UserRepository;
import com.marques.estoque.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll() {
        log.info("Buscando todos os usuários");
        List<User> userList = userRepository.findAll();
        return userMapper.toDTOList(userList);
    }

    @Transactional
    public UserResponseDTO update(Long id, UserCreateDTO userCreateDTO) {
        log.info("Atualizando usuário com id: {}", id);
        User user = returnUserWithId(id);

        String name = normalizeString(userCreateDTO.getName());
        String username = normalizeString(userCreateDTO.getUsername());

        validateForUpdate(id, name, username);

        user.setName(name);
        user.setUsername(username);

        String rawPassword = userCreateDTO.getPassword();
        if (rawPassword != null && !rawPassword.isBlank()) {
            user.setPassword(passwordEncoder.encode(rawPassword.trim()));
        }

        if (userCreateDTO.getRole() != null) {
            user.setRole(userCreateDTO.getRole());
        }

        return userMapper.toUserResponseDTO(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        User user = returnUserWithId(id);

        if (getCurrentUser().getId().equals(id)) {
            log.error("Exclusão bloqueada: usuário {} tentou excluir a própria conta", id);
            throw new ArgumentException("Não é permitido excluir a própria conta.");
        }

        if ("ADMIN".equalsIgnoreCase(user.getRole().toString())) {
            log.error("Exclusão bloqueada: tentativa de excluir o administrador id {}", id);
            throw new ArgumentException("Não é permitido excluir contas com privilégio de administrador.");
        }

        userRepository.delete(user);
    }

    /*
    -------------FUNÇÕES AUXILIARES-------------
    */

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private User returnUserWithId(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário com o id " + id + " não encontrado"));
    }

    private String normalizeString(String value) {
        if (value == null) return null;
        return value.trim();
    }

    private void validateForUpdate(Long id, String name, String username) {
        if (name == null || name.isBlank()) {
            log.error("Tentativa de atualização com nome em branco");
            throw new ArgumentException("O nome do usuário não pode ser vazio");
        }

        if (username == null || username.isBlank()) {
            log.error("Tentativa de atualização com username em branco");
            throw new ArgumentException("O username não pode ser vazio");
        }

        var existingUser = userRepository.findByUsernameIgnoreCase(username);
        if (existingUser.isPresent() && !existingUser.get().getId().equals(id)) {
            log.error("username já em uso");
            throw new DuplicateDataException("O username '" + username + "' já está em uso por outro usuário");
        }
    }
}