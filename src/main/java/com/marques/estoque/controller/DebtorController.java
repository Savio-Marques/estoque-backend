package com.marques.estoque.controller;


import com.marques.estoque.dto.DebtorDTO;
import com.marques.estoque.dto.SummaryDebtorsDTO;
import com.marques.estoque.service.DebtorService;
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
@RequestMapping("/debtor")
@AllArgsConstructor
@Tag(name = "Devedores", description = "Operações para gerenciar devedores")
public class DebtorController {

    @Autowired
    private final DebtorService debtorService;

    @Operation(summary = "Busca um devedor por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devedor retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum devedor encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<DebtorDTO> findById(@Parameter(description = "ID do devedor a ser buscado") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(debtorService.findById(id));
    }

    @Operation(summary = "Busca por nome de devedor do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devedor retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum devedor encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/pesquisar")
    public ResponseEntity<DebtorDTO> findByName(@Parameter(description = "Nome do devedor a ser buscado") @RequestParam String name) {
        return ResponseEntity.ok().body(debtorService.findByName(name));
    }

    @Operation(summary = "Lista todos os devedores do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de devedores retornada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum devedor encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping
    public ResponseEntity<List<DebtorDTO>> findAll() {
        return ResponseEntity.ok().body(debtorService.findAll());
    }

    @Operation(summary = "Cadastro de devedor do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Devedor cadastrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @PostMapping
    public ResponseEntity<DebtorDTO> save(@Valid @RequestBody DebtorDTO debtorDTO, UriComponentsBuilder uriComponentsBuilder) {
        DebtorDTO debtor = debtorService.save(debtorDTO);

        URI uri = uriComponentsBuilder.path("/product/{id}").buildAndExpand(debtor.getId()).toUri();

        return ResponseEntity.created(uri).body(debtor);
    }

    @Operation(summary = "Atualização completa de devedor do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devedor atualizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Campos em falta"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Devedor não encontrado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<DebtorDTO> update(@Valid @RequestBody DebtorDTO debtorDTO,
                                             @Parameter(description = "ID do devedor a ser buscado") @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(debtorService.update(id, debtorDTO));
    }


    @Operation(summary = "Exclusão de devedor do usuário logado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Devedor deletado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "404", description = "Devedor não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@Parameter(description = "ID do devedor a ser buscado") @PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(debtorService.delete(id));
    }

    @Operation(summary = "Sumário de devedores")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sumário retornado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Nenhum valor encontrado"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    @GetMapping("/summary")
    public ResponseEntity<SummaryDebtorsDTO> summaryDebtors() {
        return ResponseEntity.ok().body(debtorService.summaryDebtors());
    }
}
