package dev.kellyson.ImportaFlow.produto;

import com.opencsv.exceptions.CsvValidationException;
import dev.kellyson.ImportaFlow.exception.BadRequestException;
import dev.kellyson.ImportaFlow.exception.ProdutoSkuDuplicadoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImportacaoProdutoService {

    private final LeitorCsvProduto leitorCsvProduto;
    private final ProdutoRepository produtoRepository;

    public ImportacaoProdutoResponse importar(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new BadRequestException("O arquivo enviado está vazio.");
        }

        try {
            InputStream conteudoArquivo = arquivo.getInputStream();

            List<Produto> produtos = leitorCsvProduto.ler(conteudoArquivo);

            if (produtos.isEmpty()) {
                throw new BadRequestException(
                        "O arquivo não possui produtos para importação."
                );
            }

            validarSkusRepetidos(produtos);

            Optional<Produto> produtoDuplicado = produtos.stream()
                    .filter(produto -> produtoRepository.existsBySku(produto.getSku()))
                    .findFirst();

            if (produtoDuplicado.isPresent()) {
                Produto produto = produtoDuplicado.get();

                throw new ProdutoSkuDuplicadoException(
                        "Já existe um produto cadastrado com o SKU "
                                + produto.getSku()
                                + "."
                );
            }

            produtoRepository.saveAll(produtos);

            return new ImportacaoProdutoResponse(
                    arquivo.getOriginalFilename(),
                    produtos.size()
            );
        } catch (IOException | CsvValidationException exception) {
            throw new RuntimeException(
                    "Não foi possível importar o arquivo.",
                    exception
            );
        }
    }

    private void validarSkusRepetidos(List<Produto> produtos) {
        Set<String> skusEncontrados = new HashSet<>();

        for (Produto produto : produtos) {
            boolean skuAdicionado = skusEncontrados.add(produto.getSku());

            if (skuAdicionado == false) {
                throw new BadRequestException(
                        "O SKU " + produto.getSku() + " está repetido no arquivo."
                );
            }
        }
    }
}
