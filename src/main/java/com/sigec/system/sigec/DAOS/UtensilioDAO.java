package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class UtensilioDAO {

    /**
     * Busca no banco de dados apenas os laboratórios que possuem utensílios ativos vinculados na tabela utensilio.
     */
    public static List<String> listarOpcoesLaboratorio() {
        Set<String> opcoes = new LinkedHashSet<>();
        opcoes.add("Todos");

        String sqlUtensiliosLabs = "SELECT DISTINCT COALESCE(l.nome_laboratorio, 'Estoque Central') AS laboratorio_atual " +
                "FROM utensilio u " +
                "LEFT JOIN laboratorio l ON u.id_laboratorio = l.id_laboratorio " +
                "WHERE u.situacao = 'A' " +
                "ORDER BY laboratorio_atual ASC";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlUtensiliosLabs);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String nome = rs.getString("laboratorio_atual");
                if (nome != null && !nome.isBlank()) {
                    opcoes.add(nome.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar opções de laboratório dos utensílios: " + e.getMessage());
        }

        return new ArrayList<>(opcoes);
    }

    /**
     * Busca no banco de dados apenas as situações/status atualmente presentes nos utensílios ativos.
     */
    public static List<String> listarOpcoesSituacao() {
        Set<String> opcoes = new LinkedHashSet<>();
        opcoes.add("Todos");

        String sqlDistinctStatus = "SELECT DISTINCT COALESCE(" +
                "(SELECT estado_atual FROM checklist_utensilio cu WHERE cu.id_utensilio = u.id_utensilio ORDER BY cu.id_checklist_utensilio DESC LIMIT 1), " +
                "'PRONTO') AS status_atual " +
                "FROM utensilio u " +
                "WHERE u.situacao = 'A' " +
                "ORDER BY status_atual ASC";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlDistinctStatus);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String status = rs.getString("status_atual");
                if (status != null && !status.isBlank()) {
                    opcoes.add(status.trim());
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao carregar opções de situação dos utensílios: " + e.getMessage());
        }

        return new ArrayList<>(opcoes);
    }

    public static List<Utensilio> listarUtensilios() {
        List<Utensilio> lista = new ArrayList<>();

        String sql = "SELECT u.id_utensilio, u.nome_utensilio, u.quantidade, " +
                "u.numero_patrimonio, u.id_categoria_utensilio, u.id_laboratorio, " +
                "COALESCE(l.nome_laboratorio, 'Estoque Central') AS laboratorio_atual, " +
                "COALESCE((SELECT estado_atual FROM checklist_utensilio cu WHERE cu.id_utensilio = u.id_utensilio ORDER BY cu.id_checklist_utensilio DESC LIMIT 1), 'PRONTO') AS status_atual " +
                "FROM utensilio u " +
                "LEFT JOIN laboratorio l ON u.id_laboratorio = l.id_laboratorio " +
                "WHERE u.situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int qtd = rs.getInt("quantidade");
                int categoria = rs.getInt("id_categoria_utensilio");
                int patrimonio = rs.getInt("numero_patrimonio");
                int idLaboratorio = rs.getInt("id_laboratorio");

                String laboratorio = rs.getString("laboratorio_atual");
                String status = rs.getString("status_atual");

                Utensilio utensilio = new Utensilio(
                        rs.getInt("id_utensilio"),
                        rs.getString("nome_utensilio"),
                        laboratorio,
                        status,
                        qtd,
                        qtd,
                        patrimonio,
                        categoria,
                        idLaboratorio
                );
                lista.add(utensilio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Erro ao listar utensílios: " + e.getMessage());
        }
        return lista;
    }

    public static boolean registrarAvaria(int idUtensilioSelecionado, String observacao, String estado) {
        String sqlSelect = "SELECT estado_atual FROM checklist_utensilio WHERE id_utensilio = ? ORDER BY id_checklist_utensilio DESC LIMIT 1";
        String sqlInsertChecklist = "INSERT INTO checklist_utensilio (id_utensilio, estado_anterior, estado_atual, observacao, data_hora_entrada) VALUES (?, ?, ?, ?, NOW())";
        String sqlInsertOcorrencia = "INSERT INTO ocorrencia (id_utensilio, observacao) VALUES (?, ?)";

        try (Connection conn = ConfigDataBase.getConnection()) {
            conn.setAutoCommit(false);

            String estadoAnterior = "PRONTO";

            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, idUtensilioSelecionado);
                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next() && rs.getString("estado_atual") != null) {
                        estadoAnterior = rs.getString("estado_atual");
                    }
                }
            }

            try (PreparedStatement stmtChecklist = conn.prepareStatement(sqlInsertChecklist)) {
                stmtChecklist.setInt(1, idUtensilioSelecionado);
                stmtChecklist.setString(2, estadoAnterior);
                stmtChecklist.setString(3, estado);
                stmtChecklist.setString(4, observacao);
                stmtChecklist.executeUpdate();
            }

            if (observacao != null && !observacao.trim().isEmpty()) {
                try (PreparedStatement stmtOcorrencia = conn.prepareStatement(sqlInsertOcorrencia)) {
                    stmtOcorrencia.setInt(1, idUtensilioSelecionado);
                    stmtOcorrencia.setString(2, observacao);
                    stmtOcorrencia.executeUpdate();
                }
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao registrar avaria: " + e.getMessage());
            return false;
        }
    }

    public static String buscarUltimoEstadoUtensilio(int idUtensilioSelecionado) {
        String sql = "SELECT estado_atual FROM checklist_utensilio WHERE id_utensilio = ? ORDER BY id_checklist_utensilio DESC LIMIT 1";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUtensilioSelecionado);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String estado = rs.getString("estado_atual");
                    return (estado != null) ? estado : "PRONTO";
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar estado do utensílio: " + e.getMessage());
        }
        return "PRONTO";
    }

    public static boolean adicionarUtensilio(Utensilio novoUtensilio) {
        String sql = "INSERT INTO utensilio (nome_utensilio, quantidade, situacao, numero_patrimonio, id_categoria_utensilio, id_laboratorio) VALUES (?, ?, 'A', ?, ?, ?)";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoUtensilio.getNome());
            stmt.setInt(2, novoUtensilio.getQtdTotal());
            stmt.setInt(3, novoUtensilio.getNumeroPatrimonio());
            stmt.setInt(4, novoUtensilio.getIdCategoria());
            stmt.setInt(5, novoUtensilio.getIdLaboratorio());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao adicionar utensílio: " + e.getMessage());
            return false;
        }
    }

    public static boolean atualizarUtensilio(Utensilio utensilio) {
        String sql = "UPDATE utensilio SET nome_utensilio = ?, quantidade = ?, numero_patrimonio = ?, id_categoria_utensilio = ? WHERE id_utensilio = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, utensilio.getNome());
            stmt.setInt(2, utensilio.getQtdTotal());
            stmt.setInt(3, utensilio.getNumeroPatrimonio());
            stmt.setInt(4, utensilio.getIdCategoria());
            stmt.setInt(5, utensilio.getIdUtensilio());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar utensílio: " + e.getMessage());
            return false;
        }
    }


    public static List<Utensilio> buscarUtensiliosPorFiltro(String termo, String status, String laboratorio, String unidade) {
        List<Utensilio> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("    SELECT u.id_utensilio, u.nome_utensilio, u.quantidade, ");
        sql.append("    u.numero_patrimonio, u.id_categoria_utensilio, u.id_laboratorio, ");
        sql.append("    COALESCE(l.nome_laboratorio, 'Estoque Central') AS laboratorio_atual, ");
        sql.append("    COALESCE((SELECT estado_atual FROM checklist_utensilio cu WHERE cu.id_utensilio = u.id_utensilio ORDER BY cu.id_checklist_utensilio DESC LIMIT 1), 'PRONTO') AS status_atual ");
        sql.append("    FROM utensilio u ");
        sql.append("    LEFT JOIN laboratorio l ON u.id_laboratorio = l.id_laboratorio ");
        sql.append("    LEFT JOIN unidade un ON l.id_unidade = un.id_unidade ");
        sql.append("    WHERE u.situacao = 'A' ");

        List<Object> parametros = new ArrayList<>();

        if (unidade != null && !unidade.equals("Todas") && !unidade.trim().isEmpty()) {
            sql.append("    AND un.nome_unidade = ? ");
            parametros.add(unidade);
        }

        sql.append(") AS base WHERE 1=1 ");

        if (termo != null && !termo.trim().isEmpty()) {
            sql.append("AND LOWER(nome_utensilio) LIKE LOWER(?) ");
            parametros.add("%" + termo.trim() + "%");
        }

        if (laboratorio != null && !laboratorio.equals("Todos") && !laboratorio.trim().isEmpty()) {
            sql.append("AND TRIM(laboratorio_atual) = TRIM(?) ");
            parametros.add(laboratorio.trim());
        }

        if (status != null && !status.equals("Todos") && !status.trim().isEmpty()) {
            if (status.equalsIgnoreCase("Disponível")) {
                sql.append("AND status_atual = 'PRONTO' ");
            } else if (status.equalsIgnoreCase("Em Manutenção")) {
                sql.append("AND status_atual = 'EM_MANUTENCAO' ");
            } else if (status.equalsIgnoreCase("Danificado")) {
                sql.append("AND status_atual = 'DANIFICADO' ");
            } else if (status.equalsIgnoreCase("Conserto")) {
                sql.append("AND status_atual IN ('EM_MANUTENCAO', 'DANIFICADO') ");
            } else {
                sql.append("AND TRIM(status_atual) = TRIM(?) ");
                parametros.add(status.trim());
            }
        }

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int qtd = rs.getInt("quantidade");
                    int categoria = rs.getInt("id_categoria_utensilio");
                    int patrimonio = rs.getInt("numero_patrimonio");
                    int idLaboratorio = rs.getInt("id_laboratorio");

                    String labAtual = rs.getString("laboratorio_atual");
                    String statusAtualDB = rs.getString("status_atual");

                    Utensilio utensilio = new Utensilio(
                            rs.getInt("id_utensilio"),
                            rs.getString("nome_utensilio"),
                            labAtual,
                            statusAtualDB,
                            qtd,
                            qtd,
                            patrimonio,
                            categoria,
                            idLaboratorio
                    );
                    lista.add(utensilio);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao filtrar utensílios: " + e.getMessage());
        }
        return lista;
    }
}
