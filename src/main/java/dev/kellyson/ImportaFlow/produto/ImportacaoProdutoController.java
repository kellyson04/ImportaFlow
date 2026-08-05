package dev.kellyson.ImportaFlow.produto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/produtos/importacoes")
@RequiredArgsConstructor
public class ImportacaoProdutoController {

    private final ImportacaoProdutoService importacaoProdutoService;

    @PostMapping
    public ResponseEntity<ImportacaoProdutoResponse> importar(@RequestParam("arquivo") MultipartFile arquivo) {
        ImportacaoProdutoResponse response = importacaoProdutoService.importar(arquivo);

        return ResponseEntity.ok(response);
    }
}
