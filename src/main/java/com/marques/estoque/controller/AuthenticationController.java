package com.marques.estoque.controller;

import com.marques.estoque.dto.LoginRequestDTO;
import com.marques.estoque.dto.LoginResponseDTO;
import com.marques.estoque.dto.ProfileUpdateDTO;
import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.dto.UserResponseDTO;
import com.marques.estoque.exception.ArgumentException;
import com.marques.estoque.exception.DuplicateDataException;
import com.marques.estoque.model.user.User;
import com.marques.estoque.model.user.UserRole;
import com.marques.estoque.repository.UserRepository;
import com.marques.estoque.security.AuthenticatedUserProvider;
import com.marques.estoque.security.TokenService;
import com.marques.estoque.util.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Endpoints para registro, login e perfil de usuários")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final UserMapper userMapper;

    @Operation(summary = "Realiza o login de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário logado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário ou senha inválidos")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO) {
        var userNamePassword = new UsernamePasswordAuthenticationToken(loginRequestDTO.getUsername(), loginRequestDTO.getPassword());
        var auth = this.authenticationManager.authenticate(userNamePassword);

        User user = (User) auth.getPrincipal();
        var token = tokenService.generateToken(user);

        return ResponseEntity.ok(new LoginResponseDTO(token, user.getName(), user.getUsername()));
    }

    @Operation(summary = "Realiza o cadastro de um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta ou dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Nome de usuário já cadastrado")
    })
    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserCreateDTO userCreateDTO) {
        if (userCreateDTO.getPassword() == null || userCreateDTO.getPassword().trim().isEmpty()) {
            throw new ArgumentException("A senha do usuário não pode ser vazia.");
        }
        if (this.userRepository.findByUsername(userCreateDTO.getUsername()) != null) {
            throw new DuplicateDataException("Nome de usuário já cadastrado.");
        }

        String encryptedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
        User newUser = new User(userCreateDTO.getName(), userCreateDTO.getUsername(), encryptedPassword, UserRole.USER);

        this.userRepository.save(newUser);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Retorna os dados do usuário autenticado")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getProfile() {
        User currentUser = authenticatedUserProvider.getCurrentUser();
        return ResponseEntity.ok(userMapper.toUserResponseDTO(currentUser));
    }

    @Operation(summary = "Atualiza nome e/ou senha do usuário autenticado")
    @PutMapping("/me")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody @Valid ProfileUpdateDTO profileDTO) {
        User currentUser = userRepository.findById(authenticatedUserProvider.getCurrentUser().getId())
                .orElseThrow(() -> new ArgumentException("Usuário não encontrado."));

        currentUser.setName(profileDTO.getName().trim());

        if (profileDTO.getNewPassword() != null && !profileDTO.getNewPassword().trim().isEmpty()) {
            if (profileDTO.getCurrentPassword() == null
                    || !passwordEncoder.matches(profileDTO.getCurrentPassword(), currentUser.getPassword())) {
                throw new ArgumentException("A senha atual está incorreta.");
            }
            currentUser.setPassword(passwordEncoder.encode(profileDTO.getNewPassword()));
        }

        User saved = userRepository.save(currentUser);
        return ResponseEntity.ok(userMapper.toUserResponseDTO(saved));
    }
}
