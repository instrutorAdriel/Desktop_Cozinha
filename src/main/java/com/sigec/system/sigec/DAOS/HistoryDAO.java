package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.UnidadeDTO;
import com.sigec.system.sigec.Constructors.CursoDTO;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acesso a Dados (DAO) para o histórico, auditoria e lista de compras.
 */
public class HistoryDAO {

    private String codMXM;
    private String nomeProduto;
    private String nomeCategoria;
    private String lote;
    private int qtdAtual;
    private int qtdMinima;
    private int qtdComprarOverride = -1;

    // 5. Lista de Compras padrão (Estoque Baixo ou Zerado)
    public static List<HistoryDAO> listarListaDeCompras(int idUnidade) {
        List<HistoryDAO> lista = new ArrayList<>();

        String sql = "SELECT i.id_insumo, p.nome_produto, c.nome_categoria, i.quantidade, i.quantidade_minima, i.lote " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE (i.quantidade <= 0 OR i.quantidade <= i.quantidade_minima) " +
                "AND i.cancelado = 'N' AND i.id_unidade = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistoryDAO alerta = new HistoryDAO();
                    alerta.setCodMXM(String.valueOf(rs.getInt("id_insumo")));
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setNomeCategoria(rs.getString("nome_categoria"));
                    alerta.setLote(rs.getString("lote"));
                    alerta.setQtdAtual(rs.getInt("quantidade"));
                    alerta.setQtdMinima(rs.getInt("quantidade_minima"));

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar lista de compras: " + e.getMessage());
        }

        return lista;
    }

    // 6. Lista de Compras por Período e Turma (Baseado nas tabelas turma_ficha e insumo_ficha)
    public static List<HistoryDAO> listarComprasPorPeriodo(int idUnidade, int idTurma, LocalDate dataInicio, LocalDate dataFim) {
        List<HistoryDAO> listaResultados = new ArrayList<>();

        // Consulta adaptada às tabelas reais do banco (turma_ficha, insumo_ficha, insumo, produto)
        String sql = "SELECT i.id_insumo, p.nome_produto, cat.nome_categoria, i.lote, " +
                "SUM(fi.quantidade) AS qtd_necessaria, " +
                "i.quantidade AS quantidade_atual, i.quantidade_minima " +
                "FROM turma_ficha tf " +
                "JOIN insumo_ficha fi ON tf.id_ficha = fi.id_ficha " +
                "JOIN produto p ON fi.id_produto = p.id_produto " +
                "JOIN insumo i ON p.id_produto = i.id_produto " +
                "LEFT JOIN categoria cat ON p.id_categoria = cat.id_categoria " +
                "WHERE tf.data BETWEEN ? AND ? " +
                "AND tf.id_turma = ? " +
                "AND i.id_unidade = ? " +
                "GROUP BY i.id_insumo, p.nome_produto, cat.nome_categoria, i.lote, i.quantidade, i.quantidade_minima";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(dataInicio));
            stmt.setDate(2, java.sql.Date.valueOf(dataFim));
            stmt.setInt(3, idTurma);
            stmt.setInt(4, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int qtdNecessaria = rs.getInt("qtd_necessaria");
                    int qtdAtual = rs.getInt("quantidade_atual");

                    // Conta solicitada: Quantidade Atual - Quantidade Necessária
                    int resultadoConta = qtdAtual - qtdNecessaria;

                    // Se o resultado for < 0, o estoque é insuficiente; adiciona à lista com o déficit
                    if (resultadoConta < 0) {
                        HistoryDAO item = new HistoryDAO();
                        item.setCodMXM(String.valueOf(rs.getInt("id_insumo")));
                        item.setNomeProduto(rs.getString("nome_produto"));
                        item.setNomeCategoria(rs.getString("nome_categoria"));
                        item.setLote(rs.getString("lote"));
                        item.setQtdAtual(qtdAtual);
                        item.setQtdMinima(rs.getInt("quantidade_minima"));

                        item.setQtdComprarOverride(Math.abs(resultadoConta));

                        listaResultados.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar compras por período: " + e.getMessage());
        }

        return listaResultados;
    }

    public int getQtdComprar() {
        if (qtdComprarOverride >= 0) {
            return qtdComprarOverride;
        }
        int diferenca = qtdMinima - qtdAtual;
        return Math.max(0, diferenca);
    }

    public void setQtdComprarOverride(int qtdComprarOverride) {
        this.qtdComprarOverride = qtdComprarOverride;
    }

    // --- Getters e Setters ---
    public String getCodMXM() { return codMXM; }
    public void setCodMXM(String codMXM) { this.codMXM = codMXM; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getQtdAtual() { return qtdAtual; }
    public void setQtdAtual(int qtdAtual) { this.qtdAtual = qtdAtual; }

    public int getQtdMinima() { return qtdMinima; }
    public void setQtdMinima(int qtdMinima) { this.qtdMinima = qtdMinima; }

    // 7. Lista as unidades ativas
    public static List<UnidadeDTO> listarUnidades() {
        List<UnidadeDTO> unidades = new ArrayList<>();
        String sql = "SELECT id_unidade, nome_unidade FROM unidade WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                unidades.add(new UnidadeDTO(rs.getInt("id_unidade"), rs.getString("nome_unidade")));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar unidades: " + e.getMessage());
        }
        return unidades;
    }

    // 8. Puxa as Turmas ativas da tabela `turma` para popular o ChoiceBox `cbCurso`[cite: 6]
    public static List<CursoDTO> listarCursos() {
        List<CursoDTO> turmas = new ArrayList<>();
        String sql = "SELECT id_turma, nome_turma FROM turma WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                turmas.add(new CursoDTO(
                        rs.getInt("id_turma"),
                        rs.getString("nome_turma")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar turmas: " + e.getMessage());
        }
        return turmas;
    }
}