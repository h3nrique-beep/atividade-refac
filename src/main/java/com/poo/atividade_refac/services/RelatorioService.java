package com.poo.atividade_refac.services;

import com.poo.atividade_refac.repositories.LivroRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RelatorioService {
    private static final Logger log = LoggerFactory.getLogger(RelatorioService.class);
    private final LivroRepository repository;

    public RelatorioService(LivroRepository repository) {
        this.repository = repository;
    }

    public void gerar() {
        log.info("Quantidade de livros: {}", repository.count());
    }
}
