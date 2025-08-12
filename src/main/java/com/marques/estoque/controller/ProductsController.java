package com.marques.estoque.controller;


import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.service.ProductService;
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
@RequestMapping("/product")
@AllArgsConstructor
@Tag(name = "Produtos", description = "Operações para gerenciar produtos e estoque")
public class ProductsController {

    @Autowired
    private final ProductService productService;

    @Operation(summary = "Busca um produto por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum produto encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> findById(@Parameter(description = "ID do produto a ser buscado") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(productService.findById(id));
    }

    @Operation(summary = "Busca por nome de produto do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum produto encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/pesquisar")
    public ResponseEntity<ProductDTO> findByName(@Parameter(description = "Nome do produto a ser buscado") @RequestParam String name) {
        return ResponseEntity.ok().body(productService.findByName(name));
    }

    @Operation(summary = "Lista todos os produtos do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de produtos retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum produto encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<List<ProductDTO>> findAll() {
        return ResponseEntity.ok().body(productService.findAll());
    }

    @Operation(summary = "Cadastro de produto do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<ProductDTO> save(@Valid @RequestBody ProductDTO productDTO, UriComponentsBuilder uriComponentsBuilder) {
        ProductDTO product = productService.save(productDTO);

        URI uri = uriComponentsBuilder.path("/product/{id}").buildAndExpand(product.getId()).toUri();

        return ResponseEntity.created(uri).body(product);
    }

    @Operation(summary = "Atualização completa de produto do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@Valid @RequestBody ProductDTO productDTO,
                                             @Parameter(description = "ID do produto a ser buscado") @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(productService.update(id, productDTO));
    }

    @Operation(summary = "Atualização parcial de produto do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<ProductDTO> updatePatch(@Valid @RequestBody ProductDTO patchDTO,
                                                  @Parameter(description = "ID do produto a ser buscado") @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(productService.updatePatch(id, patchDTO));
    }

    @Operation(summary = "Exclusão de produto do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@Parameter(description = "ID do produto a ser buscado") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(productService.delete(id));
    }
}
