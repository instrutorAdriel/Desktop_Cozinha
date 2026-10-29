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
        // Busca os utensílios ativos no banco
        String sql = "SELECT id_utensilio, nome_utensilio, quantidade, situacao FROM utensilio WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int qtd = rs.getInt("quantidade");

                Utensilio utensilio = new Utensilio(
                        rs.getInt("id_utensilio"),
                        rs.getString("nome_utensilio"),
                        "Estoque Central",
                        "Disponível",
                        qtd,
                        qtd
                );
                lista.add(utensilio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao listar utensílios.");
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
}