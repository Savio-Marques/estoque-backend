package com.marques.estoque.controller;


import com.marques.estoque.dto.ProductDTO;
import com.marques.estoque.service.ProductService;
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
public class ProductsController {

    @Autowired
    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> findById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(productService.findById(id));
    }

    @GetMapping
    @RequestMapping("/pesquisar")
    public ResponseEntity<ProductDTO> findByName(String name) {
        return ResponseEntity.ok().body(productService.findByName(name));
    }

    @GetMapping
    public ResponseEntity<List<ProductDTO>> findAll() {
        return ResponseEntity.ok().body(productService.findAll());
    }

    @PostMapping
    public ResponseEntity<ProductDTO> save(@Valid @RequestBody ProductDTO productDTO, UriComponentsBuilder uriComponentsBuilder) {
        ProductDTO product = productService.save(productDTO);

        URI uri = uriComponentsBuilder.path("/product/{id}").buildAndExpand(product.getId()).toUri();

        return ResponseEntity.created(uri).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@Valid @RequestBody ProductDTO productDTO, @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(productService.update(id, productDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProductDTO> updatePatch(@Valid @RequestBody ProductDTO patchDTO, @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(productService.updatePatch(id, patchDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(productService.delete(id));
    }
}
