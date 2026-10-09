package com.poo.atividade_refac.discounts;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DescontoConfig {
    @Bean
    PoliticaDesconto romance() {
        return new DescontoPercentual("ROMANCE", 0.1);
    }

    @Bean
    PoliticaDesconto tecnologia() {
        return new DescontoPercentual("TECNOLOGIA", 0.2);
    }

    @Bean
    PoliticaDesconto infantil() {
        return new DescontoPercentual("INFANTIL", 0.3);
    }

    @Bean
    PoliticaDesconto hq() {
        return new DescontoPercentual("HQ", 0.4);
    }
}
