package com.marques.estoque.service;

import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.dto.UserResponseDTO;
import com.marques.estoque.exception.NotFoundException;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.UserRepository;
import com.marques.estoque.util.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserResponseDTO> findAll() {
        List<User> userList = userRepository.findAll();

        if(userList.isEmpty()) {
            log.warn("Nenhum usuário encontrado");
            throw new NotFoundException("Nenhum usuário encontrado");
        }

        return UserMapper.INSTANCE.toDTOList(userList);
    }

    public UserResponseDTO update(Long id, UserCreateDTO userCreateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário com o id: " + id + " não encontrado"));

        user.setName(userCreateDTO.getName());
        user.setUsername(userCreateDTO.getUsername());

        if(userCreateDTO.getPassword() != null && !userCreateDTO.getPassword().trim().isEmpty()) {
            String encryptedPassword = new BCryptPasswordEncoder().encode(userCreateDTO.getPassword());
            user.setPassword(encryptedPassword);
        }

        user.setRole(userCreateDTO.getRole());

        return UserMapper.INSTANCE.toUserResponseDTO(userRepository.save(user));
    }

    public String delete(Long id) {
        log.info("Deletando id: {} do banco de dados", id);
        if (!userRepository.existsById(id)){
            log.error("Usuário não encontrado com o id: {}",id);
            throw new NotFoundException("Usuário com o id " + id + " não encontrado");
        }

        userRepository.deleteById(id);
        return "Usuário com id " + id + " deletado com sucesso";
    }

    private User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
