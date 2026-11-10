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

    // 2. Lista de Compras por Período e Turma (Exclusivo para quando há datas e turma válidas)
    public static List<HistoryDAO> listarComprasPorPeriodo(int idUnidade, int idTurma, LocalDate dataInicio, LocalDate dataFim) {
        List<HistoryDAO> listaResultados = new ArrayList<>();

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
                "AND i.cancelado = 'N' " +
                "GROUP BY i.id_insumo, p.nome_produto, cat.nome_categoria, i.lote, i.quantidade, i.quantidade_minima";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(dataInicio));
            stmt.setDate(2, java.sql.Date.valueOf(dataFim));
            stmt.setInt(3, idTurma);
            stmt.setInt(4, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double qtdNecessaria = rs.getDouble("qtd_necessaria");
                    double qtdAtual = rs.getDouble("quantidade_atual");
                    double qtdMinima = rs.getDouble("quantidade_minima");
                    double quantidadeAposAlocacoes = qtdAtual - qtdNecessaria;
                    double qtdComprar = Math.max(0, qtdMinima - quantidadeAposAlocacoes);

                    if (qtdComprar > 0) {
                        HistoryDAO item = new HistoryDAO();
                        item.setCodMXM(String.valueOf(rs.getInt("id_insumo")));
                        item.setNomeProduto(rs.getString("nome_produto"));
                        item.setNomeCategoria(rs.getString("nome_categoria"));
                        item.setLote(rs.getString("lote"));
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

    // 3. Lista apenas as unidades vinculadas ao usuário logado (via laboratórios e turmas do usuário)
    public static List<UnidadeDTO> listarUnidadesDoUsuario(String nomeUsuario) {
        List<UnidadeDTO> unidades = new ArrayList<>();
        String sql = "SELECT DISTINCT u.id_unidade, u.nome_unidade " +
                "FROM unidade u " +
                "JOIN laboratorio l ON u.id_unidade = l.id_unidade " +
                "JOIN turma t ON l.id_laboratorio = t.id_laboratorio " +
                "JOIN usuario_turma ut ON t.id_turma = ut.id_turma " +
                "JOIN usuario us ON ut.id_usuario = us.id_usuario " +
                "WHERE us.nome_usuario = ? AND u.situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unidades.add(new UnidadeDTO(rs.getInt("id_unidade"), rs.getString("nome_unidade")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar unidades do usuário: " + e.getMessage());
        }

        // Fallback: se o usuário não tiver turmas vinculadas, traz todas as unidades ativas para não bloquear a tela
        if (unidades.isEmpty()) {
            unidades = listarUnidadesGeral();
        }

        return unidades;
    }

    private static List<UnidadeDTO> listarUnidadesGeral() {
        List<UnidadeDTO> unidades = new ArrayList<>();
        String sql = "SELECT id_unidade, nome_unidade FROM unidade WHERE situacao = 'A'";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                unidades.add(new UnidadeDTO(rs.getInt("id_unidade"), rs.getString("nome_unidade")));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar unidades gerais: " + e.getMessage());
        }
        return unidades;
    }

    // 4. Lista as turmas ativas para o ChoiceBox cbCurso
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