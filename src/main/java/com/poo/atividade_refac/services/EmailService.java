package com.poo.atividade_refac.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public void enviarPromocional() {
        // Preserva a simulação do projeto original; não envia mensagens reais.
        log.info("Email promocional enviado (simulação)");
    }
}
