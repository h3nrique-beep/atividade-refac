package com.poo.atividade_refac.controllers;

import com.poo.atividade_refac.contracts.LivroOperations;
import com.poo.atividade_refac.entities.Livro;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/livros")
public class LivroController {
    private final LivroOperations livros;

    public LivroController(LivroOperations livros) {
        this.livros = livros;
    }

    @GetMapping
    public List<Livro> listar() {
        return livros.listar();
    }

    @PostMapping
    public Livro cadastrar(@RequestBody Livro livro) {
        return livros.cadastrar(livro);
    }

    @PutMapping("/{id}")
    public Livro atualizar(@PathVariable Long id, @RequestBody Livro livro) {
        return livros.atualizar(id, livro);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        livros.remover(id);
    }
}
