package com.poo.atividade_refac.contracts;

import com.poo.atividade_refac.entities.Livro;
import java.util.List;

public interface LivroOperations {
    List<Livro> listar();
    Livro cadastrar(Livro livro);
    Livro atualizar(Long id, Livro livro);
    void remover(Long id);
}
