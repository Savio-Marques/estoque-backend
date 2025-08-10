package com.marques.estoque.controller;

import com.marques.estoque.dto.LoginResponseDTO;
import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.UserRepository;
import com.marques.estoque.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        var userNamePassword = new UsernamePasswordAuthenticationToken(userCreateDTO.getUsername(), userCreateDTO.getPassword());
        var auth = this.authenticationManager.authenticate(userNamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        if(this.userRepository.findByUsername(userCreateDTO.getUsername()) != null) throw new DuplicateDataException("Nome de usuário já cadastrado.");

        String encryptedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
        User newUser = new User(userCreateDTO.getName() ,userCreateDTO.getUsername(), encryptedPassword, userCreateDTO.getRole());

        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
