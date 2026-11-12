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

public class HistoryDAO {

    private String codMXM;
    private String nomeProduto;
    private String nomeCategoria;
    private String lote;
    private double qtdAtual;
    private double qtdMinima;
    private double qtdComprarOverride = -1;

    // 1. Lista de Compras padrão (Estoque Baixo ou Zerado)
    public static List<HistoryDAO> listarListaDeCompras(int idUnidade) {
        return listarListaDeCompras(idUnidade, 0);
    }

    public static List<HistoryDAO> listarListaDeCompras(int idUnidade, int idTurma) {
        List<HistoryDAO> lista = new ArrayList<>();

        String sql = "SELECT i.id_insumo, p.nome_produto, c.nome_categoria, i.quantidade, i.quantidade_minima, i.lote " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE (i.quantidade <= 0 OR i.quantidade <= i.quantidade_minima) " +
                "AND i.cancelado = 'N' AND i.id_unidade = ? " +
                "AND (? <= 0 OR EXISTS (" +
                "SELECT 1 FROM turma_ficha tf " +
                "JOIN insumo_ficha fi ON tf.id_ficha = fi.id_ficha " +
                "WHERE tf.id_turma = ? AND fi.id_produto = i.id_produto))";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);
            stmt.setInt(2, idTurma);
            stmt.setInt(3, idTurma);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistoryDAO alerta = new HistoryDAO();
                    alerta.setCodMXM(String.valueOf(rs.getInt("id_insumo")));
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setNomeCategoria(rs.getString("nome_categoria"));
                    alerta.setLote(rs.getString("lote"));
                    alerta.setQtdAtual(rs.getDouble("quantidade"));
                    alerta.setQtdMinima(rs.getDouble("quantidade_minima"));

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar lista de compras: " + e.getMessage());
        }

        return lista;
    }

    // 2. Lista de compras por período, opcionalmente filtrada por turma.
    public static List<HistoryDAO> listarComprasPorPeriodo(int idUnidade, int idTurma, LocalDate dataInicio, LocalDate dataFim) {
        List<HistoryDAO> listaResultados = new ArrayList<>();

        String sql = "SELECT MIN(i.id_insumo) AS id_insumo, p.nome_produto, cat.nome_categoria, " +
                "SUM(i.quantidade) AS quantidade_atual, SUM(i.quantidade_minima) AS quantidade_minima, " +
                "MAX(aloc.qtd_necessaria) AS qtd_necessaria " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN categoria cat ON p.id_categoria = cat.id_categoria " +
                "JOIN (SELECT fi.id_produto, SUM(fi.quantidade) AS qtd_necessaria " +
                "FROM turma_ficha tf " +
                "JOIN insumo_ficha fi ON tf.id_ficha = fi.id_ficha " +
                "WHERE tf.data BETWEEN ? AND ? AND (? <= 0 OR tf.id_turma = ?) " +
                "GROUP BY fi.id_produto) aloc ON aloc.id_produto = p.id_produto " +
                "WHERE i.id_unidade = ? AND i.cancelado = 'N' " +
                "GROUP BY p.id_produto, p.nome_produto, cat.nome_categoria";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(dataInicio));
            stmt.setDate(2, java.sql.Date.valueOf(dataFim));
            stmt.setInt(3, idTurma);
            stmt.setInt(4, idTurma);
            stmt.setInt(5, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double qtdNecessaria = rs.getDouble("qtd_necessaria");
                    double qtdAtual = rs.getDouble("quantidade_atual");
                    double qtdMinima = rs.getDouble("quantidade_minima");
                    double qtdComprar = Math.max(0, qtdMinima - (qtdAtual - qtdNecessaria));

                    if (qtdComprar > 0) {
                        HistoryDAO item = new HistoryDAO();
                        item.setCodMXM(String.valueOf(rs.getInt("id_insumo")));
                        item.setNomeProduto(rs.getString("nome_produto"));
                        item.setNomeCategoria(rs.getString("nome_categoria"));
                        item.setQtdAtual(qtdAtual);
                        item.setQtdMinima(qtdMinima);
                        item.setQtdComprarOverride(qtdComprar);

                        listaResultados.add(item);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar compras por período: " + e.getMessage());
        }

        return listaResultados;
    }

    public double getQtdComprar() {
        if (qtdComprarOverride >= 0) {
            return qtdComprarOverride;
        }
        return Math.max(0, qtdMinima - qtdAtual);
    }

    public void setQtdComprarOverride(double qtdComprarOverride) {
        this.qtdComprarOverride = qtdComprarOverride;
    }

    // Getters e Setters
    public String getCodMXM() { return codMXM; }
    public void setCodMXM(String codMXM) { this.codMXM = codMXM; }
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    public double getQtdAtual() { return qtdAtual; }
    public void setQtdAtual(double qtdAtual) { this.qtdAtual = qtdAtual; }
    public double getQtdMinima() { return qtdMinima; }
    public void setQtdMinima(double qtdMinima) { this.qtdMinima = qtdMinima; }

    // 3. Lista somente as unidades associadas ao usuário na tabela usuario_unidade.
    public static List<UnidadeDTO> listarUnidadesDoUsuario(int idUsuario) {
        List<UnidadeDTO> unidades = new ArrayList<>();
        String sql = "SELECT DISTINCT u.id_unidade, u.nome_unidade " +
                "FROM unidade u " +
                "JOIN usuario_unidade uu ON u.id_unidade = uu.id_unidade " +
                "WHERE uu.id_usuario = ? AND u.situacao = 'A' " +
                "ORDER BY u.nome_unidade";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unidades.add(new UnidadeDTO(rs.getInt("id_unidade"), rs.getString("nome_unidade")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar unidades do usuário: " + e.getMessage());
        }

        return unidades;
    }

    // 4. Lista turmas da unidade com fichas que usam insumos cadastrados naquela unidade.
    public static List<CursoDTO> listarCursos(int idUnidade) {
        List<CursoDTO> turmas = new ArrayList<>();
        String sql = "SELECT DISTINCT t.id_turma, t.nome_turma " +
                "FROM turma t " +
                "JOIN laboratorio l ON t.id_laboratorio = l.id_laboratorio " +
                "WHERE t.situacao = 'A' AND l.id_unidade = ? " +
                "AND EXISTS (SELECT 1 FROM turma_ficha tf " +
                "JOIN insumo_ficha fi ON tf.id_ficha = fi.id_ficha " +
                "JOIN insumo i ON fi.id_produto = i.id_produto " +
                "WHERE tf.id_turma = t.id_turma AND i.id_unidade = l.id_unidade " +
                "AND i.cancelado = 'N') " +
                "ORDER BY t.nome_turma";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUnidade);
            try (ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    turmas.add(new CursoDTO(
                            rs.getInt("id_turma"),
                            rs.getString("nome_turma")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar turmas: " + e.getMessage());
        }
        return turmas;
    }
}