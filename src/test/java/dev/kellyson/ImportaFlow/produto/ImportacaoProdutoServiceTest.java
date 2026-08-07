package dev.kellyson.ImportaFlow.produto;

import dev.kellyson.ImportaFlow.exception.BadRequestException;
import dev.kellyson.ImportaFlow.exception.ProdutoSkuDuplicadoException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportacaoProdutoServiceTest {

    @Mock
    private LeitorCsvProduto leitorCsvProduto;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private MultipartFile arquivo;

    @InjectMocks
    private ImportacaoProdutoService importacaoProdutoService;

    @Test
    void deveRejeitarArquivoVazio() {
        when(arquivo.isEmpty()).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> importacaoProdutoService.importar(arquivo)
        );

        assertEquals("O arquivo enviado está vazio.", exception.getMessage());
        verifyNoInteractions(leitorCsvProduto, produtoRepository);
    }

    @Test
    void deveRejeitarArquivoSemProdutos() throws Exception {
        InputStream conteudoArquivo = InputStream.nullInputStream();
        when(arquivo.getInputStream()).thenReturn(conteudoArquivo);
        when(leitorCsvProduto.ler(conteudoArquivo)).thenReturn(List.of());

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> importacaoProdutoService.importar(arquivo)
        );

        assertEquals("O arquivo não possui produtos para importação.", exception.getMessage());
        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveRejeitarSkuRepetidoNoArquivo() throws Exception {
        InputStream conteudoArquivo = InputStream.nullInputStream();
        Produto primeiroProduto = criarProduto("MOU-001");
        Produto segundoProduto = criarProduto("MOU-001");
        when(arquivo.getInputStream()).thenReturn(conteudoArquivo);
        when(leitorCsvProduto.ler(conteudoArquivo))
                .thenReturn(List.of(primeiroProduto, segundoProduto));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> importacaoProdutoService.importar(arquivo)
        );

        assertEquals("O SKU MOU-001 está repetido no arquivo.", exception.getMessage());
        verifyNoInteractions(produtoRepository);
    }

    @Test
    void deveRejeitarSkuJaExistenteNoBanco() throws Exception {
        InputStream conteudoArquivo = InputStream.nullInputStream();
        Produto produto = criarProduto("MOU-001");
        when(arquivo.getInputStream()).thenReturn(conteudoArquivo);
        when(leitorCsvProduto.ler(conteudoArquivo)).thenReturn(List.of(produto));
        when(produtoRepository.existsBySku("MOU-001")).thenReturn(true);

        ProdutoSkuDuplicadoException exception = assertThrows(
                ProdutoSkuDuplicadoException.class,
                () -> importacaoProdutoService.importar(arquivo)
        );

        assertEquals("Já existe um produto cadastrado com o SKU MOU-001.", exception.getMessage());
        verify(produtoRepository, never()).saveAll(any());
    }

    @Test
    void deveSalvarProdutosERetornarResumo() throws Exception {
        InputStream conteudoArquivo = InputStream.nullInputStream();
        Produto primeiroProduto = criarProduto("MOU-001");
        Produto segundoProduto = criarProduto("TEC-001");
        List<Produto> produtos = List.of(primeiroProduto, segundoProduto);
        when(arquivo.getInputStream()).thenReturn(conteudoArquivo);
        when(arquivo.getOriginalFilename()).thenReturn("produtos.csv");
        when(leitorCsvProduto.ler(conteudoArquivo)).thenReturn(produtos);
        when(produtoRepository.existsBySku("MOU-001")).thenReturn(false);
        when(produtoRepository.existsBySku("TEC-001")).thenReturn(false);

        ImportacaoProdutoResponse response = importacaoProdutoService.importar(arquivo);

        assertEquals("produtos.csv", response.nomeArquivo());
        assertEquals(2, response.produtosImportados());
        verify(produtoRepository).saveAll(produtos);
    }

    private Produto criarProduto(String sku) {
        return new Produto(sku, "Produto de teste", new BigDecimal("10.00"), 5);
    }
}
