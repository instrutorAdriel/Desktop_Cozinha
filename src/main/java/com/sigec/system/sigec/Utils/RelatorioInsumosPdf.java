package com.sigec.system.sigec.Utils;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.sigec.system.sigec.DAOS.RelatorioInsumosDAO.InsumoNecessario;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gera o PDF "Previsão de Insumos para as Aulas" (biblioteca OpenPDF).
 * Cores e fontes seguem o StyleSheet.css do projeto.
 */
public final class RelatorioInsumosPdf {

    /** Dados do cabeçalho do relatório (os filtros usados para gerar a lista). */
    public record Cabecalho(String unidade, String curso, String turma,
                            LocalDate inicio, LocalDate fim, String geradoPor) {
    }

    // Paleta do StyleSheet.css
    private static final Color AZUL_SENAC = new Color(0x05, 0x45, 0x84);   // #054584
    private static final Color LARANJA    = new Color(0xe4, 0x89, 0x1b);   // #e4891b
    private static final Color TEXTO      = new Color(0x1e, 0x29, 0x3b);   // #1e293b
    private static final Color CINZA      = new Color(0x64, 0x74, 0x8b);   // #64748b
    private static final Color ZEBRA      = new Color(0xf8, 0xfa, 0xfc);   // #f8fafc
    private static final Color LINHA      = new Color(0xe2, 0xe8, 0xf0);   // #e2e8f0

    // Helvetica embutida no PDF: cobre acentos do português (á, ã, ç...)
    private static final Font F_TITULO   = new Font(Font.HELVETICA, 15, Font.BOLD, AZUL_SENAC);
    private static final Font F_SISTEMA  = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL_SENAC);
    private static final Font F_ROTULO   = new Font(Font.HELVETICA, 9, Font.BOLD, CINZA);
    private static final Font F_VALOR    = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO);
    private static final Font F_CABECALHO = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    private static final Font F_CELULA   = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO);
    private static final Font F_RODAPE   = new Font(Font.HELVETICA, 8, Font.NORMAL, CINZA);

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private RelatorioInsumosPdf() {
    }

    public static void gerar(File destino, Cabecalho cab, List<InsumoNecessario> itens)
            throws IOException, DocumentException {

        Document doc = new Document(PageSize.A4, 36, 36, 36, 50);
        try (OutputStream out = new FileOutputStream(destino)) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            String geradoEm = LocalDateTime.now().format(DATA_HORA);
            writer.setPageEvent(new Rodape("Gerado por " + cab.geradoPor() + " em " + geradoEm));

            doc.addTitle("Previsão de Insumos para as Aulas");
            doc.addCreator("S.I.G.E.C");
            doc.open();

            adicionarTopo(doc);
            adicionarFiltros(doc, cab, itens.size());
            adicionarTabela(doc, itens);

            doc.close();
        }
    }

    // ------------------------------------------------------------------
    // Logo + nome do sistema + título, com uma linha laranja embaixo
    // ------------------------------------------------------------------
    private static void adicionarTopo(Document doc) throws DocumentException {
        PdfPTable topo = new PdfPTable(new float[]{1, 1});
        topo.setWidthPercentage(100);

        PdfPCell esquerda = celulaSemBorda();
        Image logo = carregarLogo();
        if (logo != null) {
            logo.scaleToFit(110, 36);
            esquerda.addElement(logo);
        }
        topo.addCell(esquerda);

        PdfPCell direita = celulaSemBorda();
        direita.setHorizontalAlignment(Element.ALIGN_RIGHT);
        direita.setVerticalAlignment(Element.ALIGN_MIDDLE);
        direita.setPhrase(new Phrase("S.I.G.E.C", F_SISTEMA));
        topo.addCell(direita);

        PdfPCell linha = celulaSemBorda();
        linha.setColspan(2);
        linha.setBorder(Rectangle.BOTTOM);
        linha.setBorderColorBottom(LARANJA);
        linha.setBorderWidthBottom(2f);
        linha.setFixedHeight(6);
        topo.addCell(linha);
        doc.add(topo);

        Paragraph titulo = new Paragraph("PREVISÃO DE INSUMOS PARA AS AULAS", F_TITULO);
        titulo.setSpacingBefore(14);
        titulo.setSpacingAfter(10);
        doc.add(titulo);
    }

    // ------------------------------------------------------------------
    // Bloco com os filtros usados (Unidade / Curso / Turma / Período / Itens)
    // ------------------------------------------------------------------
    private static void adicionarFiltros(Document doc, Cabecalho cab, int totalItens) throws DocumentException {
        PdfPTable filtros = new PdfPTable(new float[]{1.5f, 1.2f, 1.2f, 1.5f, 0.6f});
        filtros.setWidthPercentage(100);
        filtros.setSpacingAfter(14);

        filtros.addCell(blocoFiltro("UNIDADE", cab.unidade()));
        filtros.addCell(blocoFiltro("CURSO", cab.curso()));
        filtros.addCell(blocoFiltro("TURMA", cab.turma()));
        filtros.addCell(blocoFiltro("PERÍODO",
                cab.inicio().format(DATA) + " a " + cab.fim().format(DATA)));
        filtros.addCell(blocoFiltro("ITENS", String.valueOf(totalItens)));
        doc.add(filtros);
    }

    private static PdfPCell blocoFiltro(String rotulo, String valor) {
        PdfPCell cell = celulaSemBorda();
        cell.setPaddingRight(8);
        Paragraph p = new Paragraph();
        p.add(new Chunk(rotulo + "\n", F_ROTULO));
        p.add(new Chunk(valor == null ? "-" : valor, F_VALOR));
        p.setLeading(13);
        cell.addElement(p);
        return cell;
    }

    // ------------------------------------------------------------------
    // Tabela: Categoria | Insumo / Produto | Qtd. Necessária | Unidade
    // ------------------------------------------------------------------
    private static void adicionarTabela(Document doc, List<InsumoNecessario> itens) throws DocumentException {
        PdfPTable tabela = new PdfPTable(new float[]{22, 45, 18, 15});
        tabela.setWidthPercentage(100);
        tabela.setHeaderRows(1); // repete o cabeçalho em cada página

        cabecalho(tabela, "Categoria", Element.ALIGN_LEFT);
        cabecalho(tabela, "Insumo / Produto", Element.ALIGN_LEFT);
        cabecalho(tabela, "Qtd. Necessária", Element.ALIGN_RIGHT);
        cabecalho(tabela, "Unidade", Element.ALIGN_LEFT);

        DecimalFormat formato = new DecimalFormat("#,##0.###",
                DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")));

        for (int i = 0; i < itens.size(); i++) {
            InsumoNecessario item = itens.get(i);
            Color fundo = (i % 2 == 0) ? Color.WHITE : ZEBRA;
            celula(tabela, item.categoria(), Element.ALIGN_LEFT, fundo);
            celula(tabela, item.nomeProduto(), Element.ALIGN_LEFT, fundo);
            celula(tabela, formato.format(item.quantidade()), Element.ALIGN_RIGHT, fundo);
            celula(tabela, item.unidade(), Element.ALIGN_LEFT, fundo);
        }
        doc.add(tabela);
    }

    private static void cabecalho(PdfPTable t, String texto, int alinhamento) {
        PdfPCell c = new PdfPCell(new Phrase(texto, F_CABECALHO));
        c.setBackgroundColor(AZUL_SENAC);
        c.setBorderColor(AZUL_SENAC);
        c.setHorizontalAlignment(alinhamento);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(7);
        t.addCell(c);
    }

    private static void celula(PdfPTable t, String texto, int alinhamento, Color fundo) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, F_CELULA));
        c.setBackgroundColor(fundo);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(LINHA);
        c.setHorizontalAlignment(alinhamento);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        t.addCell(c);
    }

    private static PdfPCell celulaSemBorda() {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        return c;
    }

    /** Usa a mesma logo das telas; se não encontrar, o PDF sai sem logo. */
    private static Image carregarLogo() {
        try {
            URL url = RelatorioInsumosPdf.class.getResource("/com/sigec/system/sigec/imagens/LogocoloridaSenac.png");
            return url == null ? null : Image.getInstance(url);
        } catch (Exception e) {
            return null;
        }
    }

    // ------------------------------------------------------------------
    // Rodapé em todas as páginas: "Gerado por ..." à esquerda, "Página N" à direita
    // ------------------------------------------------------------------
    private static final class Rodape extends PdfPageEventHelper {
        private final String texto;

        Rodape(String texto) {
            this.texto = texto;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 22;

            cb.setColorStroke(LINHA);
            cb.setLineWidth(0.8f);
            cb.moveTo(document.left(), y + 12);
            cb.lineTo(document.right(), y + 12);
            cb.stroke();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(texto, F_RODAPE), document.left(), y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), F_RODAPE), document.right(), y, 0);
        }
    }
}