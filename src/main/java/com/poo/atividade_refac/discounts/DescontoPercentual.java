package com.poo.atividade_refac.discounts;

public record DescontoPercentual(String categoria, double percentual) implements PoliticaDesconto {
    public DescontoPercentual {
        if (categoria == null || categoria.isBlank()
                || !Double.isFinite(percentual) || percentual < 0 || percentual >= 1) {
            throw new IllegalArgumentException("Categoria e percentual de desconto inválidos");
        }
    }

    @Override
    public double aplicar(double preco) {
        return preco * (1 - percentual);
    }
}
