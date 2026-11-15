package com.marques.estoque.service;

import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.dto.UserResponseDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.CategoryRepository;
import com.marques.estoque.repository.DebtorRepository;
import com.marques.estoque.repository.ProductRepository;
import com.marques.estoque.repository.StockMovementRepository;
import com.marques.estoque.repository.UserRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.util.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DebtorRepository debtorRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public List<UserResponseDTO> findAll() {
        List<User> userList = userRepository.findAll();

        if (userList.isEmpty()) {
            log.warn("Nenhum usuário encontrado");
            throw new NotFoundException("Nenhum usuário encontrado");
        }

        return userMapper.toDTOList(userList);
    }

    public UserResponseDTO update(Long id, UserCreateDTO userCreateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário com o id: " + id + " não encontrado"));

        if (userRepository.existsByUsernameAndIdNot(userCreateDTO.getUsername(), id)) {
            throw new DuplicateDataException("Nome de usuário já cadastrado.");
        }

        User currentUser = authenticatedUserProvider.getCurrentUser();
        if (currentUser.getId().equals(id) && userCreateDTO.getRole() == UserRole.USER) {
            throw new ArgumentException("Você não pode remover seu próprio privilégio de administrador.");
        }

        user.setName(userCreateDTO.getName().trim());
        user.setUsername(userCreateDTO.getUsername().trim());

        if (userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().trim().isEmpty()) {
            if (userCreateDTO.getPassword().trim().length() < 6) {
                throw new ArgumentException("A nova senha deve ter pelo menos 6 caracteres.");
            }
            String encryptedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
            user.setPassword(encryptedPassword);
        }

        if (userCreateDTO.getRole() != null) {
            user.setRole(userCreateDTO.getRole());
        }

        return userMapper.toUserResponseDTO(userRepository.save(user));
    }

    @Transactional
    public String delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        User currentUser = authenticatedUserProvider.getCurrentUser();
        if (currentUser.getId().equals(id)) {
            throw new ArgumentException("Você não pode excluir sua própria conta.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Usuário não encontrado com o id: {}", id);
                    return new NotFoundException("Usuário com o id " + id + " não encontrado");
                });

        productRepository.deleteAllByUser(user);
        categoryRepository.deleteAllByUser(user);
        debtorRepository.deleteAllByUser(user);
        stockMovementRepository.deleteAllByUser(user);
        userRepository.delete(user);

        return "Usuário com id " + id + " deletado com sucesso";
    }
}
