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
    private String unidadeMedida;
    private String lote;
    private double qtdAtual;
    private double qtdMinima;
    private double qtdComprarOverride;

    // Lista os insumos necessários para todas as aulas agendadas, sem filtro de período.
    public static List<HistoryDAO> listarListaDeCompras(int idUnidade) {
        return listarListaDeCompras(idUnidade, 0);
    }

    public static List<HistoryDAO> listarListaDeCompras(int idUnidade, int idCurso) {
        return listarComprasNecessarias(idUnidade, idCurso, null, null);
    }

    // Lista apenas os insumos necessários para as aulas agendadas no período selecionado.
    public static List<HistoryDAO> listarComprasPorPeriodo(int idUnidade, int idCurso, LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null || dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Informe um período válido para consultar as compras.");
        }
        return listarComprasNecessarias(idUnidade, idCurso, dataInicio, dataFim);
    }


    private static List<HistoryDAO> listarComprasNecessarias(
            int idUnidade, int idCurso, LocalDate dataInicio, LocalDate dataFim) {
        List<HistoryDAO> resultados = new ArrayList<>();
        boolean filtrarPeriodo = dataInicio != null;

        String sql = "SELECT p.codigo_mxm, p.nome_produto, " +
                "p.unidade_m AS unidade_m, c.nome_categoria, " +
                "COALESCE(estoque.quantidade_atual, 0) AS quantidade_atual, demanda.qtd_necessaria " +
                "FROM (" +
                "SELECT fi.id_produto, SUM(fi.quantidade) AS qtd_necessaria " +
                "FROM agendamento a " +
                "JOIN turma t ON t.id_turma = a.id_turma " +
                "JOIN laboratorio l ON l.id_laboratorio = t.id_laboratorio " +
                "JOIN insumo_ficha fi ON fi.id_ficha = a.id_ficha " +
                "WHERE l.id_unidade = ? " +
                "AND a.situacao = 'A' " +        // ignora agendamentos inativados
                "AND a.concluido = 'N' " +       // aula já dada não gera compra
                (filtrarPeriodo ? "AND a.data >= ? AND a.data < ? " : "") +
                "AND (? <= 0 OR t.id_curso = ?) " +
                "GROUP BY fi.id_produto" +
                ") demanda " +
                "JOIN produto p ON p.id_produto = demanda.id_produto " +
                "LEFT JOIN categoria c ON c.id_categoria = p.id_categoria " +
                "LEFT JOIN (" +
                "SELECT i.id_produto, SUM(COALESCE(i.quantidade, 0)) AS quantidade_atual " +
                "FROM insumo i " +
                "WHERE i.id_unidade = ? AND i.cancelado = 'N' " +
                "GROUP BY i.id_produto" +
                ") estoque ON estoque.id_produto = demanda.id_produto " +
                "WHERE demanda.qtd_necessaria > COALESCE(estoque.quantidade_atual, 0) " +
                "ORDER BY c.nome_categoria, p.nome_produto";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int parametro = 1;
            stmt.setInt(parametro++, idUnidade);
            if (filtrarPeriodo) {
                stmt.setDate(parametro++, java.sql.Date.valueOf(dataInicio));
                stmt.setDate(parametro++, java.sql.Date.valueOf(dataFim.plusDays(1)));
            }
            stmt.setInt(parametro++, idCurso);
            stmt.setInt(parametro++, idCurso);
            stmt.setInt(parametro, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    double qtdAtual = rs.getDouble("quantidade_atual");
                    double qtdComprar = rs.getDouble("qtd_necessaria") - qtdAtual;
                    HistoryDAO item = new HistoryDAO();
                    item.setCodMXM(rs.getString("codigo_mxm")); // agora existe produto.codigo_mxm
                    item.setNomeProduto(rs.getString("nome_produto"));
                    item.setUnidadeMedida(rs.getString("unidade_m"));
                    item.setNomeCategoria(rs.getString("nome_categoria"));
                    item.setQtdAtual(qtdAtual);
                    item.setQtdComprarOverride(qtdComprar);
                    resultados.add(item);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Erro ao listar compras necessárias: " + e.getMessage(), e);
        }

        return resultados;
    }

    public double getQtdComprar() {
        return qtdComprarOverride;
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
    public String getUnidadeMedida() { return unidadeMedida; }
    public void setUnidadeMedida(String unidadeMedida) { this.unidadeMedida = unidadeMedida; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    public double getQtdAtual() { return qtdAtual; }
    public void setQtdAtual(double qtdAtual) { this.qtdAtual = qtdAtual; }
    public double getQtdMinima() { return qtdMinima; }
    public void setQtdMinima(double qtdMinima) { this.qtdMinima = qtdMinima; }

    // Lista somente as unidades associadas ao usuário na tabela usuario_unidade.
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

    // Lista todos os cursos ativos para o filtro do relatório.
    public static List<CursoDTO> listarCursosAtivos() {
        List<CursoDTO> cursos = new ArrayList<>();
        String sql = "SELECT c.id_curso, c.nome_curso " +
                "FROM curso c " +
                "WHERE c.situacao = 'A' " +
                "ORDER BY c.nome_curso";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cursos.add(new CursoDTO(
                            rs.getInt("id_curso"),
                            rs.getString("nome_curso")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar cursos: " + e.getMessage());
        }
        return cursos;
    }
}