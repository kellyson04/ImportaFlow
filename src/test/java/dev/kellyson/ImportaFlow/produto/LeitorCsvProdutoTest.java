package dev.kellyson.ImportaFlow.produto;

import dev.kellyson.ImportaFlow.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LeitorCsvProdutoTest {

    private LeitorCsvProduto leitorCsvProduto;

    @BeforeEach
    void preparar() {
        leitorCsvProduto = new LeitorCsvProduto();
    }

    @Test
    void deveLerProdutoValido() throws Exception {
        String csv = "sku,nome,preco,estoque\nMOU-003,Mouse Gamer,349.90,15";
        InputStream conteudoArquivo = criarInputStream(csv);

        List<Produto> produtos = leitorCsvProduto.ler(conteudoArquivo);

        assertEquals(1, produtos.size());
        assertEquals("MOU-003", produtos.get(0).getSku());
        assertEquals("Mouse Gamer", produtos.get(0).getNome());
        assertEquals(new BigDecimal("349.90"), produtos.get(0).getPreco());
        assertEquals(15, produtos.get(0).getEstoque());
    }

    @Test
    void deveRejeitarLinhaComQuantidadeIncorretaDeColunas() {
        String csv = "sku,nome,preco,estoque\nMOU-003,Mouse Gamer,349.90";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("Cada linha do CSV deve possuir exatamente 4 colunas.", exception.getMessage());
    }

    @Test
    void deveRejeitarSkuVazio() {
        String csv = "sku,nome,preco,estoque\n,Mouse Gamer,349.90,15";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("O SKU não pode estar vazio.", exception.getMessage());
    }

    @Test
    void deveRejeitarNomeVazio() {
        String csv = "sku,nome,preco,estoque\nMOU-003,,349.90,15";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("O nome não pode estar vazio.", exception.getMessage());
    }

    @Test
    void deveRejeitarPrecoInvalido() {
        String csv = "sku,nome,preco,estoque\nMOU-003,Mouse Gamer,CARO,15";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("O preço \"CARO\" é inválido.", exception.getMessage());
    }

    @Test
    void deveRejeitarPrecoNegativo() {
        String csv = "sku,nome,preco,estoque\nMOU-003,Mouse Gamer,-10.00,15";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("O preço não pode ser negativo.", exception.getMessage());
    }

    @Test
    void deveRejeitarEstoqueInvalido() {
        String csv = "sku,nome,preco,estoque\nMOU-003,Mouse Gamer,349.90,muito";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("O estoque \"muito\" é inválido.", exception.getMessage());
    }

    @Test
    void deveRejeitarEstoqueNegativo() {
        String csv = "sku,nome,preco,estoque\nMOU-003,Mouse Gamer,349.90,-5";
        InputStream conteudoArquivo = criarInputStream(csv);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> leitorCsvProduto.ler(conteudoArquivo)
        );

        assertEquals("O estoque não pode ser negativo.", exception.getMessage());
    }

    private InputStream criarInputStream(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}
