package com.poo.atividade_refac.services;

import com.poo.atividade_refac.contracts.LivroOperations;
import com.poo.atividade_refac.discounts.PoliticaDesconto;
import com.poo.atividade_refac.entities.Livro;
import com.poo.atividade_refac.repositories.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class LivroService implements LivroOperations {
    private final LivroRepository repository;
    private final Map<String, PoliticaDesconto> descontos;
    private final RelatorioService relatorio;
    private final EmailService email;

    public LivroService(LivroRepository repository, List<PoliticaDesconto> descontos,
                        RelatorioService relatorio, EmailService email) {
        this.repository = repository;
        this.descontos = descontos.stream().collect(
                Collectors.toMap(PoliticaDesconto::categoria, Function.identity()));
        this.relatorio = relatorio;
        this.email = email;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Livro> listar() {
        return repository.findAll();
    }

    @Override
    public Livro cadastrar(Livro livro) {
        validar(livro);
        livro.setId(null);
        PoliticaDesconto desconto = descontos.get(livro.getCategoria());
        if (desconto != null) {
            livro.setPreco(desconto.aplicar(livro.getPreco()));
        }
        Livro salvo = repository.save(livro);
        relatorio.gerar();
        email.enviarPromocional();
        return salvo;
    }

    @Override
    public Livro atualizar(Long id, Livro livro) {
        Livro existente = buscar(id);
        validar(livro);
        existente.setTitulo(livro.getTitulo());
        existente.setAutor(livro.getAutor());
        existente.setPreco(livro.getPreco());
        existente.setCategoria(livro.getCategoria());
        return repository.save(existente);
    }

    @Override
    public void remover(Long id) {
        Livro livro = buscar(id);
        if ("RARO".equals(livro.getCategoria())) {
            throw new IllegalStateException("Livros raros não podem ser removidos");
        }
        repository.delete(livro);
    }

    private Livro buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Livro não encontrado: " + id));
    }

    private void validar(Livro livro) {
        if (livro == null || livro.getTitulo() == null || livro.getTitulo().isBlank()) {
            throw new IllegalArgumentException("Título obrigatório");
        }
        if (livro.getPreco() == null || !Double.isFinite(livro.getPreco()) || livro.getPreco() <= 0) {
            throw new IllegalArgumentException("Preço inválido");
        }
    }
}
