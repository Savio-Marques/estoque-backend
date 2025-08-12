package com.marques.estoque.controller;

import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/category")
@AllArgsConstructor
@Tag(name = "Categorias", description = "Operações para gerenciar categorias de produtos")
public class CategoryController {

    @Autowired
    private final CategoryService categoryService;

    @Operation(summary = "Busca uma categoria por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma categoria encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> findById(@Parameter(description = "ID da categoria a ser buscada") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(categoryService.findById(id));
    }

    @Operation(summary = "Busca uma categoria por nome")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma categoria encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/pesquisar")
    public ResponseEntity<CategoryDTO> findByName(@Parameter(description = "Nome da categoria a ser pesquisada") @RequestParam String name) {
        return ResponseEntity.ok().body(categoryService.findByName(name));
    }

    @Operation(summary = "Lista todas as categorias do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de categorias retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhuma categoria encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll() {
        return ResponseEntity.ok().body(categoryService.findAll());
    }

    @Operation(summary = "Cadastra uma nova categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta ou dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "409", description = "Categoria já existente")
    })
    @PostMapping
    public ResponseEntity<CategoryDTO> save(@RequestBody @Valid CategoryDTO categoryDTO, UriComponentsBuilder uriComponentsBuilder) {
        CategoryDTO category = categoryService.save(categoryDTO);
        URI uri = uriComponentsBuilder.path("/category/{id}").buildAndExpand(category.getId()).toUri();
        return ResponseEntity.created(uri).body(category);
    }

    @Operation(summary = "Atualização completa de uma categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta ou dados inválidos"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@RequestBody @Valid CategoryDTO categoryDTO,
                                              @Parameter(description = "ID da categoria a ser atualizada") @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(categoryService.update(id, categoryDTO));
    }

    @Operation(summary = "Exclusão de uma categoria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria deletada com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@Parameter(description = "ID da categoria a ser deletada") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(categoryService.delete(id));
    }
}