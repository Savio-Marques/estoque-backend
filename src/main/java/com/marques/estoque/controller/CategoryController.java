package com.marques.estoque.controller;


import com.marques.estoque.dto.CategoryDTO;
import com.marques.estoque.service.CategoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/category")
@AllArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> findById(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(categoryService.findById(id));
    }

    @GetMapping
    @RequestMapping("/pesquisar")
    public ResponseEntity<CategoryDTO> findByName(String name) {
        return ResponseEntity.ok().body(categoryService.findByName(name));
    }

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> findAll() {
        return ResponseEntity.ok().body(categoryService.findAll());
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> save(@RequestBody CategoryDTO categoryDTO, UriComponentsBuilder uriComponentsBuilder) {
        CategoryDTO category = categoryService.save(categoryDTO);

        URI uri = uriComponentsBuilder.path("/category/{id}").buildAndExpand(category.getId()).toUri();

        return ResponseEntity.created(uri).body(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDTO> update(@RequestBody CategoryDTO categoryDTO, @PathVariable(name = "id") Long id){
        return ResponseEntity.ok().body(categoryService.update(id, categoryDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok().body(categoryService.delete(id));
    }
}
