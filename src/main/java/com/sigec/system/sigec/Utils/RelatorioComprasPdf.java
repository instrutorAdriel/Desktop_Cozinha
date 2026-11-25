package com.sigec.system.sigec.Utils;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfPTable;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.ItemCompra;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * PDF "Lista de Compras" (tela de Relatório).
 * Traz só os itens que precisam ser comprados; o visual comum fica em { PdfSigec}.
 */
public final class RelatorioComprasPdf {

    private RelatorioComprasPdf() {
    }

    /**
     * @param itens todos os itens da tabela; o PDF lista apenas os que têm "A comprar" maior que zero
     */
    public static void gerar(File destino, PdfSigec.Filtros filtros, List<ItemCompra> itens)
            throws IOException, DocumentException {

        List<ItemCompra> comprar = itens.stream().filter(ItemCompra::precisaComprar).toList();

        // A4 paisagem: 7 colunas não cabem em retrato sem quebrar códigos e títulos
        PdfSigec.gerar(destino, "Lista de Compras", filtros.geradoPor(), true, doc -> {
            // Lista da unidade inteira: só Unidade e Período (sem Curso/Turma)
            PdfSigec.adicionarFiltrosUnidade(doc, filtros,
                    new String[]{"ITENS A COMPRAR", comprar.size() + " de " + itens.size()},
                    new String[]{"ESTOQUE CONSIDERADO", "Validade conferida por aula"});

            int esq = Element.ALIGN_LEFT, dir = Element.ALIGN_RIGHT;
            PdfPTable tabela = PdfSigec.novaTabela(
                    // "Unidade de Medida" precisa de mais largura para não quebrar em duas linhas
                    new float[]{11, 13, 28, 11, 11, 11, 15},
                    new String[]{"Cód. MXM", "Categoria", "Insumo / Produto", "Necessário", "Em estoque", "A comprar", "Unidade de Medida"},
                    new int[]{esq, esq, esq, dir, dir, dir, esq});

            for (int i = 0; i < comprar.size(); i++) {
                ItemCompra item = comprar.get(i);
                PdfSigec.celula(tabela, item.codigoMxm() == null ? "—" : item.codigoMxm(), esq, i, PdfSigec.F_CELULA);
                PdfSigec.celula(tabela, item.categoria(), esq, i, PdfSigec.F_CELULA);
                PdfSigec.celula(tabela, item.nomeProduto(), esq, i, PdfSigec.F_CELULA);
                PdfSigec.celula(tabela, PdfSigec.numero(item.necessario()), dir, i, PdfSigec.F_CELULA);
                PdfSigec.celula(tabela, PdfSigec.numero(item.emEstoque()), dir, i, PdfSigec.F_CELULA);
                PdfSigec.celula(tabela, PdfSigec.numero(item.aComprar()), dir, i, PdfSigec.F_CELULA_FORTE);
                PdfSigec.celula(tabela, item.unidade(), esq, i, PdfSigec.F_CELULA);
            }
            doc.add(tabela);

            PdfSigec.adicionarNota(doc, "A comprar = necessário para as aulas não concluídas + estoque mínimo - estoque aproveitável. "
                    + "Cada lote não cancelado da unidade só atende as aulas que ocorrem até a sua data de validade "
                    + "(lotes sem validade atendem todas), usando primeiro o que vence antes. "
                    + "Estoque aproveitável = o que as aulas consomem + a sobra ainda válida em "
                    + filtros.fim().format(PdfSigec.DATA) + ".");
        });
    }
}