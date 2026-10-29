package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtensilioDAO {

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
                int idLaboratorio = rs.getInt("id_laboratorio"); // Extraindo o ID do laboratório

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
        // Query para descobrir o estado atual antes da mudança
        String sqlSelect = "SELECT estado_atual FROM checklist_utensilio WHERE id_utensilio = ? ORDER BY id_checklist_utensilio DESC LIMIT 1";

        // Query para inserir o novo registro com o novo estado na checklist
        String sqlInsertChecklist = "INSERT INTO checklist_utensilio (id_utensilio, estado_anterior, estado_atual, observacao, data_hora_entrada) VALUES (?, ?, ?, ?, NOW())";

        // Query para registrar na tabela 'ocorrencia' (histórico formal de avarias)
        String sqlInsertOcorrencia = "INSERT INTO ocorrencia (id_utensilio, observacao) VALUES (?, ?)";

        try (Connection conn = ConfigDataBase.getConnection()) {
            // Desativa o auto-commit para garantir que ou salva em ambas as tabelas, ou em nenhuma
            conn.setAutoCommit(false);

            String estadoAnterior = "PRONTO";

            //  Busca o estado anterior
            try (PreparedStatement stmtSelect = conn.prepareStatement(sqlSelect)) {
                stmtSelect.setInt(1, idUtensilioSelecionado);
                try (ResultSet rs = stmtSelect.executeQuery()) {
                    if (rs.next() && rs.getString("estado_atual") != null) {
                        estadoAnterior = rs.getString("estado_atual");
                    }
                }
            }

            //  Insere na tabela checklist_utensilio (para atualizar o status de PRONTO/MANUTENÇÃO/DANIFICADO)
            try (PreparedStatement stmtChecklist = conn.prepareStatement(sqlInsertChecklist)) {
                stmtChecklist.setInt(1, idUtensilioSelecionado);
                stmtChecklist.setString(2, estadoAnterior);
                stmtChecklist.setString(3, estado);
                stmtChecklist.setString(4, observacao);
                stmtChecklist.executeUpdate();
            }

            //  Insere na tabela ocorrencia (histórico de descrições das avarias)
            // Apenas insere se houver uma observação digitada
            if (observacao != null && !observacao.trim().isEmpty()) {
                try (PreparedStatement stmtOcorrencia = conn.prepareStatement(sqlInsertOcorrencia)) {
                    stmtOcorrencia.setInt(1, idUtensilioSelecionado);
                    stmtOcorrencia.setString(2, observacao);
                    stmtOcorrencia.executeUpdate();
                }
            }

            // Confirma a transação no banco
            conn.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao registrar avaria: " + e.getMessage());
            return false;
        }
    }

    public static String buscarUltimoEstadoUtensilio(int idUtensilioSelecionado) {
        // Busca o último registro de checklist deste utensílio para saber seu estado atual
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
        // Se o utensílio for novo e nunca teve avaria, o padrão é PRONTO
        return "PRONTO";
    }

    public static boolean adicionarUtensilio(Utensilio novoUtensilio) {
        // Agora o INSERT inclui o id_laboratorio
        String sql = "INSERT INTO utensilio (nome_utensilio, quantidade, situacao, numero_patrimonio, id_categoria_utensilio, id_laboratorio) VALUES (?, ?, 'A', ?, ?, ?)";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novoUtensilio.getNome());
            stmt.setInt(2, novoUtensilio.getQtdTotal());
            stmt.setInt(3, novoUtensilio.getNumeroPatrimonio());
            stmt.setInt(4, novoUtensilio.getIdCategoria());
            stmt.setInt(5, novoUtensilio.getIdLaboratorio()); // Enviando o laboratório para o banco

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

    public static List<Utensilio> buscarUtensiliosPorFiltro(String termo, String status, String laboratorio) {
        List<Utensilio> lista = new ArrayList<>();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("    SELECT u.id_utensilio, u.nome_utensilio, u.quantidade, ");
        sql.append("    u.numero_patrimonio, u.id_categoria_utensilio, u.id_laboratorio, ");
        sql.append("    COALESCE(l.nome_laboratorio, 'Estoque Central') AS laboratorio_atual, ");
        sql.append("    COALESCE((SELECT estado_atual FROM checklist_utensilio cu WHERE cu.id_utensilio = u.id_utensilio ORDER BY cu.id_checklist_utensilio DESC LIMIT 1), 'PRONTO') AS status_atual ");
        sql.append("    FROM utensilio u ");
        sql.append("    LEFT JOIN laboratorio l ON u.id_laboratorio = l.id_laboratorio ");
        sql.append("    WHERE u.situacao = 'A' ");
        sql.append(") AS base WHERE 1=1 ");

        List<Object> parametros = new ArrayList<>();

        // Filtro por Nome (Pesquisa)
        if (termo != null && !termo.trim().isEmpty()) {
            sql.append("AND LOWER(nome_utensilio) LIKE LOWER(?) ");
            parametros.add("%" + termo.trim() + "%");
        }

        // Filtro por Laboratório
        if (laboratorio != null && !laboratorio.equals("Todos") && !laboratorio.trim().isEmpty()) {
            sql.append("AND laboratorio_atual = ? ");
            parametros.add(laboratorio);
        }

        if (status != null && !status.equals("Todos") && !status.trim().isEmpty()) {
            if (status.equals("Disponível")) {
                sql.append("AND status_atual = 'PRONTO' ");
            } else if (status.equals("Conserto")) {
                sql.append("AND status_atual IN ('EM_MANUTENCAO', 'DANIFICADO') ");
            } else {
                sql.append("AND status_atual = ? ");
                parametros.add(status);
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
                    String statusVisual = statusAtualDB.equals("PRONTO") ? "Disponível" :
                            statusAtualDB.equals("EM_MANUTENCAO") ? "Em Manutenção" :
                                    statusAtualDB.equals("DANIFICADO") ? "Danificado" : statusAtualDB;

                    Utensilio utensilio = new Utensilio(
                            rs.getInt("id_utensilio"),
                            rs.getString("nome_utensilio"),
                            labAtual,
                            statusVisual,
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