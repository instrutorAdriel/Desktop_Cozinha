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
import java.util.Locale;

/**
 * Base visual comum dos relatórios em PDF do SIGEC (biblioteca OpenPDF):
 * topo com logo, título, bloco de filtros, tabela no padrão do sistema e rodapé.
 * Cores e fontes seguem o StyleSheet.css do projeto.
 */
public final class PdfSigec {

    /** Filtros usados para gerar o relatório (aparecem no topo do PDF). */
    public record Filtros(String unidade, String curso, String turma,
                          LocalDate inicio, LocalDate fim, String geradoPor) {
    }

    /** Conteúdo específico de cada relatório, escrito depois do topo. */
    @FunctionalInterface
    public interface Conteudo {
        void escrever(Document doc) throws DocumentException;
    }

    // Paleta do StyleSheet.css
    public static final Color AZUL_SENAC = new Color(0x05, 0x45, 0x84);   // #054584
    public static final Color LARANJA    = new Color(0xe4, 0x89, 0x1b);   // #e4891b
    public static final Color TEXTO      = new Color(0x1e, 0x29, 0x3b);   // #1e293b
    public static final Color CINZA      = new Color(0x64, 0x74, 0x8b);   // #64748b
    public static final Color ZEBRA      = new Color(0xf8, 0xfa, 0xfc);   // #f8fafc
    public static final Color LINHA      = new Color(0xe2, 0xe8, 0xf0);   // #e2e8f0

    // Helvetica embutida no PDF: cobre acentos do português (á, ã, ç...)
    public static final Font F_TITULO       = new Font(Font.HELVETICA, 15, Font.BOLD, AZUL_SENAC);
    public static final Font F_SISTEMA      = new Font(Font.HELVETICA, 13, Font.BOLD, AZUL_SENAC);
    public static final Font F_ROTULO       = new Font(Font.HELVETICA, 9, Font.BOLD, CINZA);
    public static final Font F_VALOR        = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO);
    public static final Font F_CABECALHO    = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
    public static final Font F_CELULA       = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXTO);
    public static final Font F_CELULA_FORTE = new Font(Font.HELVETICA, 10, Font.BOLD, TEXTO);
    public static final Font F_NOTA         = new Font(Font.HELVETICA, 8, Font.NORMAL, CINZA);

    public static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter DATA_ARQUIVO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private PdfSigec() {
    }

    // ------------------------------------------------------------------
    // Documento: A4, topo, conteúdo e rodapé "Gerado por ... / Página N"
    // ------------------------------------------------------------------
    public static void gerar(File destino, String titulo, String geradoPor, Conteudo conteudo)
            throws IOException, DocumentException {
        gerar(destino, titulo, geradoPor, false, conteudo);
    }

    /** @param paisagem true = A4 deitado (para tabelas com muitas colunas) */
    public static void gerar(File destino, String titulo, String geradoPor, boolean paisagem, Conteudo conteudo)
            throws IOException, DocumentException {

        Document doc = new Document(paisagem ? PageSize.A4.rotate() : PageSize.A4, 36, 36, 36, 50);
        try (OutputStream out = new FileOutputStream(destino)) {
            PdfWriter writer = PdfWriter.getInstance(doc, out);
            String geradoEm = LocalDateTime.now().format(DATA_HORA);
            writer.setPageEvent(new Rodape("Gerado por " + geradoPor + " em " + geradoEm));

            doc.addTitle(titulo);
            doc.addCreator("S.I.G.E.C");
            doc.open();

            adicionarTopo(doc, titulo);
            conteudo.escrever(doc);

            doc.close();
        }
    }

    /** Logo + "S.I.G.E.C" + linha laranja + título. */
    private static void adicionarTopo(Document doc, String titulo) throws DocumentException {
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

        Paragraph p = new Paragraph(titulo.toUpperCase(Locale.forLanguageTag("pt-BR")), F_TITULO);
        p.setSpacingBefore(14);
        p.setSpacingAfter(10);
        doc.add(p);
    }

    // ------------------------------------------------------------------
    // Filtros em duas linhas (nomes longos não quebram em 3 linhas):
    //   Unidade | Curso | Turma
    //   Período | extra1 | extra2
    // ------------------------------------------------------------------
    public static void adicionarFiltros(Document doc, Filtros f, String[] extra1, String[] extra2)
            throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1, 1, 1});
        t.setWidthPercentage(100);
        t.setSpacingAfter(14);

        t.addCell(blocoFiltro("UNIDADE", f.unidade()));
        t.addCell(blocoFiltro("CURSO", f.curso()));
        t.addCell(blocoFiltro("TURMA", f.turma()));
        t.addCell(blocoFiltro("PERÍODO", f.inicio().format(DATA) + " a " + f.fim().format(DATA)));
        t.addCell(extra1 == null ? celulaSemBorda() : blocoFiltro(extra1[0], extra1[1]));
        t.addCell(extra2 == null ? celulaSemBorda() : blocoFiltro(extra2[0], extra2[1]));
        doc.add(t);
    }

    // ------------------------------------------------------------------
    // Filtros só da unidade  em uma linha:
    //   Unidade | Período | extra1 | extra2
    // ------------------------------------------------------------------
    public static void adicionarFiltrosUnidade(Document doc, Filtros f, String[] extra1, String[] extra2)
            throws DocumentException {
        PdfPTable t = new PdfPTable(new float[]{1.6f, 1, 0.8f, 1});
        t.setWidthPercentage(100);
        t.setSpacingAfter(14);

        t.addCell(blocoFiltro("UNIDADE", f.unidade()));
        t.addCell(blocoFiltro("PERÍODO", f.inicio().format(DATA) + " a " + f.fim().format(DATA)));
        t.addCell(extra1 == null ? celulaSemBorda() : blocoFiltro(extra1[0], extra1[1]));
        t.addCell(extra2 == null ? celulaSemBorda() : blocoFiltro(extra2[0], extra2[1]));
        doc.add(t);
    }

    private static PdfPCell blocoFiltro(String rotulo, String valor) {
        PdfPCell cell = celulaSemBorda();
        cell.setPaddingRight(10);
        cell.setPaddingBottom(6);
        Paragraph p = new Paragraph();
        p.add(new Chunk(rotulo + "\n", F_ROTULO));
        p.add(new Chunk(valor == null ? "-" : valor, F_VALOR));
        p.setLeading(13);
        cell.addElement(p);
        return cell;
    }

    // ------------------------------------------------------------------
    // Tabela no padrão do sistema (cabeçalho azul, zebra, cabeçalho repetido em cada página)
    // ------------------------------------------------------------------
    public static PdfPTable novaTabela(float[] pesos, String[] titulos, int[] alinhamentos) {
        PdfPTable t = new PdfPTable(pesos);
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (int i = 0; i < titulos.length; i++) {
            PdfPCell c = new PdfPCell(new Phrase(titulos[i], F_CABECALHO));
            c.setBackgroundColor(AZUL_SENAC);
            c.setBorderColor(AZUL_SENAC);
            c.setHorizontalAlignment(alinhamentos[i]);
            c.setVerticalAlignment(Element.ALIGN_MIDDLE);
            c.setPadding(7);
            t.addCell(c);
        }
        return t;
    }

    public static void celula(PdfPTable t, String texto, int alinhamento, int linha, Font fonte) {
        PdfPCell c = new PdfPCell(new Phrase(texto == null ? "" : texto, fonte));
        c.setBackgroundColor(linha % 2 == 0 ? Color.WHITE : ZEBRA);
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColor(LINHA);
        c.setHorizontalAlignment(alinhamento);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(6);
        t.addCell(c);
    }

    /** Observação em letra pequena abaixo da tabela. */
    public static void adicionarNota(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, F_NOTA);
        p.setSpacingBefore(8);
        doc.add(p);
    }

    /** 1.200 / 0,5 / 3 (padrão brasileiro, até 3 casas). */
    public static String numero(double valor) {
        return new DecimalFormat("#,##0.###", DecimalFormatSymbols.getInstance(Locale.forLanguageTag("pt-BR")))
                .format(valor);
    }

    // ------------------------------------------------------------------
    // Onde salvar: Documentos\SIGEC\Relatórios (criada se não existir).
    // Evita o Windows salvar em pastas ocultas quando a janela abre em "Este Computador".
    // ------------------------------------------------------------------
    public static File pastaRelatorios() {
        File documentos = javax.swing.filechooser.FileSystemView.getFileSystemView().getDefaultDirectory();
        if (documentos == null || !documentos.isDirectory()) {
            documentos = new File(System.getProperty("user.home"));
        }
        File pasta = new File(new File(documentos, "SIGEC"), "Relatórios");
        if (!pasta.isDirectory() && !pasta.mkdirs()) {
            return documentos; // sem permissão para criar: usa Documentos
        }
        return pasta;
    }

    /** Ex.: nomeArquivo("lista-compras", filtros) -> lista-compras_16-11-2026_a_19-11-2026.pdf */
    public static String nomeArquivo(String prefixo, Filtros f) {
        return prefixo + "_" + f.inicio().format(DATA_ARQUIVO) + "_a_" + f.fim().format(DATA_ARQUIVO) + ".pdf";
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
            URL url = PdfSigec.class.getResource("/com/sigec/system/sigec/imagens/LogocoloridaSenac.png");
            return url == null ? null : Image.getInstance(url);
        } catch (Exception e) {
            return null;
        }
    }

    /** Rodapé em todas as páginas: "Gerado por ..." à esquerda, "Página N" à direita. */
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
                    new Phrase(texto, F_NOTA), document.left(), y, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Página " + writer.getPageNumber(), F_NOTA), document.right(), y, 0);
        }
    }
}