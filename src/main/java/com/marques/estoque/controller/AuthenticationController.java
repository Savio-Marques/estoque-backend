package com.marques.estoque.controller;

import com.marques.estoque.dto.LoginResponseDTO;
import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.model.user.User;
import com.marques.estoque.repository.UserRepository;
import com.marques.estoque.security.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints para registro e login de usuários")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Realiza o login de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário logado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        var userNamePassword = new UsernamePasswordAuthenticationToken(userCreateDTO.getUsername(), userCreateDTO.getPassword());
        var auth = this.authenticationManager.authenticate(userNamePassword);

        var token = tokenService.generateToken((User) auth.getPrincipal());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @Operation(summary = "Realiza o cadastro de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "409", description = "Nome de usuário já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        if(this.userRepository.findByUsername(userCreateDTO.getUsername()) != null) throw new DuplicateDataException("Nome de usuário já cadastrado.");

        String encryptedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
        User newUser = new User(userCreateDTO.getName() ,userCreateDTO.getUsername(), encryptedPassword, userCreateDTO.getRole());

        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }
}
