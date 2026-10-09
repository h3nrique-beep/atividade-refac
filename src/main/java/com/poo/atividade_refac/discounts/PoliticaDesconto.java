package com.poo.atividade_refac.discounts;

public interface PoliticaDesconto {
    String categoria();
    double aplicar(double preco);
}
