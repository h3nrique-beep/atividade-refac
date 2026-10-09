package com.poo.atividade_refac;

import com.poo.atividade_refac.contracts.LivroOperations;
import com.poo.atividade_refac.discounts.DescontoPercentual;
import com.poo.atividade_refac.discounts.PoliticaDesconto;
import com.poo.atividade_refac.entities.Livro;
import com.poo.atividade_refac.repositories.LivroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(LivroApiTests.DescontoTesteConfig.class)
class LivroApiTests {
    @Autowired MockMvc mvc;
    @Autowired LivroRepository repository;
    @Autowired LivroOperations livros;

    @BeforeEach
    void limparBanco() {
        repository.deleteAll();
    }

    @ParameterizedTest
    @CsvSource({"ROMANCE,90", "TECNOLOGIA,80", "INFANTIL,70", "HQ,60", "OUTRA,100", "ESPECIAL,95"})
    void cadastrarAplicaDescontosEPermiteNovaEstrategia(String categoria, double preco) throws Exception {
        mvc.perform(post("/livros").contentType("application/json")
                        .content(corpo("Livro", "100", "\"" + categoria + "\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.preco").value(preco));
        assertEquals(preco, repository.findAll().getFirst().getPreco(), 0.000001);
    }

    @Test
    void fluxoCompletoPreservaContratoDaApi() throws Exception {
        mvc.perform(get("/livros")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(post("/livros").contentType("application/json")
                        .content(corpo("Clean Code", "120", "\"TECNOLOGIA\"")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(96.0));
        Long id = repository.findAll().getFirst().getId();
        mvc.perform(get("/livros")).andExpect(jsonPath("$[0].titulo").value("Clean Code"));
        mvc.perform(put("/livros/{id}", id).contentType("application/json")
                        .content(corpo("Clean Architecture", "150", "\"TECNOLOGIA\"")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.titulo").value("Clean Architecture"))
                .andExpect(jsonPath("$.autor").value("Autor"))
                .andExpect(jsonPath("$.categoria").value("TECNOLOGIA"))
                .andExpect(jsonPath("$.preco").value(150.0));
        assertEquals(150.0, repository.findById(id).orElseThrow().getPreco());
        mvc.perform(delete("/livros/{id}", id)).andExpect(status().isOk());
        assertEquals(0, repository.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "0", "-1"})
    void rejeitaPrecoInvalidoNoCadastroENaAtualizacao(String preco) throws Exception {
        Livro existente = repository.save(new Livro(null, "Original", "Autor", 100.0, "OUTRA"));
        String json = corpo("Livro", preco, "\"OUTRA\"");
        mvc.perform(post("/livros").contentType("application/json").content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Preço inválido"));
        mvc.perform(put("/livros/{id}", existente.getId()).contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
        assertEquals(1, repository.count());
        Livro preservado = repository.findById(existente.getId()).orElseThrow();
        assertEquals(100.0, preservado.getPreco());
        assertEquals("Original", preservado.getTitulo());
    }

    @ParameterizedTest
    @ValueSource(strings = {"null", "\"\"", "\"   \""})
    void rejeitaTituloInvalido(String titulo) throws Exception {
        Livro existente = repository.save(new Livro(null, "Original", "Autor", 100.0, "OUTRA"));
        String json = "{\"titulo\":" + titulo + ",\"preco\":100}";
        mvc.perform(post("/livros").contentType("application/json").content(json))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("Título obrigatório"));
        mvc.perform(put("/livros/{id}", existente.getId()).contentType("application/json").content(json))
                .andExpect(status().isBadRequest());
        assertEquals("Original", repository.findById(existente.getId()).orElseThrow().getTitulo());
        assertEquals(1, repository.count());
    }

    @Test
    void retorna404ParaLivroInexistente() throws Exception {
        mvc.perform(put("/livros/999999").contentType("application/json")
                        .content(corpo("Livro", "100", "null")))
                .andExpect(status().isNotFound());
        mvc.perform(delete("/livros/999999")).andExpect(status().isNotFound());
    }

    @Test
    void naoRemoveLivroRaro() throws Exception {
        Livro raro = repository.save(new Livro(null, "Raro", "Autor", 100.0, "RARO"));
        mvc.perform(delete("/livros/{id}", raro.getId()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Livros raros não podem ser removidos"));
        assertTrue(repository.existsById(raro.getId()));
    }

    @Test
    void cadastroIgnoraIdInformadoEAceitaCategoriaNulaSemDesconto() throws Exception {
        Livro original = repository.save(new Livro(null, "Original", "Autor", 50.0, "OUTRA"));
        mvc.perform(post("/livros").contentType("application/json")
                        .content("{\"id\":" + original.getId() + ",\"titulo\":\"Novo\",\"preco\":100}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.preco").value(100.0));
        assertEquals(2, repository.count());
        assertEquals("Original", repository.findById(original.getId()).orElseThrow().getTitulo());
    }

    @Test
    void validaValoresNaoFinitosEConfiguracaoDeDesconto() {
        for (double preco : new double[]{Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> livros.cadastrar(new Livro(null, "Livro", "Autor", preco, "OUTRA")));
        }
        for (double percentual : new double[]{Double.NaN, Double.POSITIVE_INFINITY, -0.1, 1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new DescontoPercentual("OUTRA", percentual));
        }
        assertThrows(IllegalArgumentException.class, () -> new DescontoPercentual(null, 0.1));
        assertThrows(IllegalArgumentException.class, () -> new DescontoPercentual(" ", 0.1));
        assertEquals(0, repository.count());
    }

    private String corpo(String titulo, String preco, String categoria) {
        return """
                {"titulo":"%s","autor":"Autor","preco":%s,"categoria":%s}
                """.formatted(titulo, preco, categoria);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class DescontoTesteConfig {
        @Bean
        PoliticaDesconto descontoEspecial() {
            return new PoliticaDesconto() {
                public String categoria() { return "ESPECIAL"; }
                public double aplicar(double preco) { return Math.max(0, preco - 5); }
            };
        }
    }
}
