package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Controllers.SolicitacoesController.SolicitacaoMock;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class SolicitacaoDAO {

    public static List<SolicitacaoMock> listarTodasSolicitacoes() {
        List<SolicitacaoMock> lista = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        // Busca o curso pela turma da solicitação OU pela turma vinculada ao usuário (usuario_turma)
        String sql =
                "SELECT DISTINCT si.id_solicitacao_insumo AS id, si.data_solicitacao AS data_hora, " +
                        "COALESCE(u.nome_usuario, 'Sem Nome') AS solicitante, " +
                        "COALESCE(c.nome_curso, c_user.nome_curso, t.nome_turma, t_user.nome_turma, 'Sem Curso') AS nome_curso, " +
                        "COALESCE(t.nome_turma, t_user.nome_turma, 'Sem Turma') AS nome_turma, " +
                        "COALESCE(un.nome_unidade, 'Todas') AS unidade, " +
                        "COALESCE(p.nome_produto, 'Insumo') AS item, " +
                        "CONCAT(si.quantidade, ' ', COALESCE(si.unidade_medida, '')) AS qtd, " +
                        "si.justificativa, si.situacao, 'INSUMO' AS tipo " +
                        "FROM solicitacao_insumo si " +
                        "LEFT JOIN usuario u ON si.id_usuario = u.id_usuario " +
                        "LEFT JOIN turma t ON si.id_turma = t.id_turma " +
                        "LEFT JOIN curso c ON t.id_curso = c.id_curso " +
                        "LEFT JOIN usuario_turma ut_u ON u.id_usuario = ut_u.id_usuario " +
                        "LEFT JOIN turma t_user ON ut_u.id_turma = t_user.id_turma " +
                        "LEFT JOIN curso c_user ON t_user.id_curso = c_user.id_curso " +
                        "LEFT JOIN laboratorio l ON COALESCE(t.id_laboratorio, t_user.id_laboratorio) = l.id_laboratorio " +
                        "LEFT JOIN unidade un ON l.id_unidade = un.id_unidade " +
                        "LEFT JOIN insumo i ON si.id_insumo = i.id_insumo " +
                        "LEFT JOIN produto p ON i.id_produto = p.id_produto " +

                        "UNION ALL " +

                        "SELECT DISTINCT su.id_solicitacao_utensilio AS id, su.data_solicitacao AS data_hora, " +
                        "COALESCE(u.nome_usuario, 'Sem Nome') AS solicitante, " +
                        "COALESCE(c.nome_curso, c_user.nome_curso, t.nome_turma, t_user.nome_turma, 'Sem Curso') AS nome_curso, " +
                        "COALESCE(t.nome_turma, t_user.nome_turma, 'Sem Turma') AS nome_turma, " +
                        "COALESCE(un.nome_unidade, 'Todas') AS unidade, " +
                        "COALESCE(ut.nome_utensilio, 'Utensílio') AS item, " +
                        "CAST(su.quantidade AS CHAR) AS qtd, " +
                        "su.justificativa, su.situacao, 'UTENSILIO' AS tipo " +
                        "FROM solicitacao_utensilio su " +
                        "LEFT JOIN usuario u ON su.id_usuario = u.id_usuario " +
                        "LEFT JOIN turma t ON su.id_turma = t.id_turma " +
                        "LEFT JOIN curso c ON t.id_curso = c.id_curso " +
                        "LEFT JOIN usuario_turma ut_u ON u.id_usuario = ut_u.id_usuario " +
                        "LEFT JOIN turma t_user ON ut_u.id_turma = t_user.id_turma " +
                        "LEFT JOIN curso c_user ON t_user.id_curso = c_user.id_curso " +
                        "LEFT JOIN laboratorio l ON COALESCE(t.id_laboratorio, t_user.id_laboratorio) = l.id_laboratorio " +
                        "LEFT JOIN unidade un ON l.id_unidade = un.id_unidade " +
                        "LEFT JOIN utensilio ut ON su.id_utensilio = ut.id_utensilio " +

                        "ORDER BY data_hora DESC";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String data = rs.getTimestamp("data_hora") != null ? sdf.format(rs.getTimestamp("data_hora")) : "";
                String solicitante = rs.getString("solicitante");
                String nomeCurso = rs.getString("nome_curso");
                String nomeTurma = rs.getString("nome_turma");
                String unidade = rs.getString("unidade");
                String item = rs.getString("item");
                String quantidade = rs.getString("qtd");
                String justificativa = rs.getString("justificativa");
                if (justificativa == null || justificativa.trim().isEmpty()) {
                    justificativa = "-";
                }
                String tipo = rs.getString("tipo");

                String situacaoBanco = rs.getString("situacao");
                String statusExtenso = "Pendente";
                if ("A".equalsIgnoreCase(situacaoBanco)) {
                    statusExtenso = "Aprovado";
                } else if ("R".equalsIgnoreCase(situacaoBanco)) {
                    statusExtenso = "Recusado";
                }

                lista.add(new SolicitacaoMock(id, data, solicitante, nomeCurso, nomeTurma, unidade, item, quantidade, justificativa, statusExtenso, tipo));
            }

        } catch (Exception e) {
            System.err.println("Erro ao carregar solicitações do banco de dados: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    public static boolean atualizarStatus(int id, String novaSituacao, String tipo) {
        String tabela = "UTENSILIO".equalsIgnoreCase(tipo) ? "solicitacao_utensilio" : "solicitacao_insumo";
        String colunaId = "UTENSILIO".equalsIgnoreCase(tipo) ? "id_solicitacao_utensilio" : "id_solicitacao_insumo";

        String sql = "UPDATE " + tabela + " SET situacao = ? WHERE " + colunaId + " = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaSituacao);
            stmt.setInt(2, id);

            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Erro ao atualizar status da solicitação: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}