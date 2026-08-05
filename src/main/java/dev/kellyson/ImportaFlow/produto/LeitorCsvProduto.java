package dev.kellyson.ImportaFlow.produto;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import dev.kellyson.ImportaFlow.exception.BadRequestException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class LeitorCsvProduto {

    public List<Produto> ler(InputStream conteudoArquivo)
            throws IOException, CsvValidationException {

        List<Produto> produtos = new ArrayList<>();

        InputStreamReader leitorArquivo = new InputStreamReader(
                conteudoArquivo,
                StandardCharsets.UTF_8
        );

        try (CSVReader leitorCsv = new CSVReader(leitorArquivo)) {
            leitorCsv.readNext();

            String[] colunas = leitorCsv.readNext();

            while (colunas != null) {
                String sku = colunas[0].trim();
                String nome = colunas[1].trim();
                BigDecimal preco = converterPreco(colunas[2]);
                Integer estoque = Integer.valueOf(colunas[3].trim());

                Produto produto = new Produto(
                        sku,
                        nome,
                        preco,
                        estoque
                );

                produtos.add(produto);

                colunas = leitorCsv.readNext();
            }
        }

        return produtos;
    }

    private BigDecimal converterPreco(String precoTexto) {
        try {
            BigDecimal preco = new BigDecimal(precoTexto.trim());

            if (preco.compareTo(BigDecimal.ZERO) < 0) {
                throw new BadRequestException("O preço não pode ser negativo.");
            }

            return preco;
        } catch (NumberFormatException exception) {
            throw new BadRequestException("O preço \"" + precoTexto + "\" é inválido.");
        }
    }
}
