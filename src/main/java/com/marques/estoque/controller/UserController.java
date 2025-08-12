package com.marques.estoque.controller;

import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.dto.UserCreateDTO;
import com.marques.estoque.dto.UserResponseDTO;
import com.marques.estoque.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "Usuários", description = "Operações para gerenciar usuários")
public class UserController {

    @Autowired
    private UserService userService;


    @Operation(summary = "Retorna uma lista de todos os usuários")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado")
    })
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> findAll() {
        return ResponseEntity.ok().body(userService.findAll());
    }

    @Operation(summary = "Atualiza usuário selecionado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta ou inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> update(@Valid @RequestBody UserCreateDTO userCreateDTO,
                                                  @Parameter(description = "ID do usuário a ser buscado") @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(userService.update(id, userCreateDTO));
    }

    @Operation(summary = "Exclusão de usuário selecionado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@Parameter(description = "ID do usuário a ser buscado") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(userService.delete(id));
    }

}