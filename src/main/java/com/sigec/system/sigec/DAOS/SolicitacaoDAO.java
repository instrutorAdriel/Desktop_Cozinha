package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Controllers.SolicitacoesController.SolicitacaoMock;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

public class SolicitacaoDAO {

    public static List<SolicitacaoMock> listarTodasSolicitacoes() {
        List<SolicitacaoMock> lista = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());

        // COALESCE(c.nome_curso, t.nome_turma, 'Sem Curso') garante que, se c.nome_curso for NULL por conta do id_curso nulo na turma, o nome da turma é exibido!
        String sql =
                "SELECT si.id_solicitacao_insumo AS id, si.data_solicitacao AS data_hora, u.nome_usuario AS solicitante, " +
                        "COALESCE(c.nome_curso, t.nome_turma, 'Sem Curso') AS curso, " +
                        "COALESCE(p.nome_produto, 'Insumo') AS item, " +
                        "CONCAT(si.quantidade, ' ', COALESCE(si.unidade_medida, '')) AS qtd, " +
                        "si.justificativa, si.situacao, 'INSUMO' AS tipo " +
                        "FROM solicitacao_insumo si " +
                        "LEFT JOIN usuario u ON si.id_usuario = u.id_usuario " +
                        "LEFT JOIN turma t ON si.id_turma = t.id_turma " +
                        "LEFT JOIN curso c ON t.id_curso = c.id_curso " +
                        "LEFT JOIN insumo i ON si.id_insumo = i.id_insumo " +
                        "LEFT JOIN produto p ON i.id_produto = p.id_produto " +

                        "UNION ALL " +

                        "SELECT su.id_solicitacao_utensilio AS id, su.data_solicitacao AS data_hora, u.nome_usuario AS solicitante, " +
                        "COALESCE(c.nome_curso, t.nome_turma, 'Sem Curso') AS curso, " +
                        "COALESCE(ut.nome_utensilio, 'Utensílio') AS item, " +
                        "CAST(su.quantidade AS CHAR) AS qtd, " +
                        "su.justificativa, su.situacao, 'UTENSILIO' AS tipo " +
                        "FROM solicitacao_utensilio su " +
                        "LEFT JOIN usuario u ON su.id_usuario = u.id_usuario " +
                        "LEFT JOIN turma t ON su.id_turma = t.id_turma " +
                        "LEFT JOIN curso c ON t.id_curso = c.id_curso " +
                        "LEFT JOIN utensilio ut ON su.id_utensilio = ut.id_utensilio " +

                        "ORDER BY data_hora DESC";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String data = rs.getTimestamp("data_hora") != null ? sdf.format(rs.getTimestamp("data_hora")) : "";
                String solicitante = rs.getString("solicitante") != null ? rs.getString("solicitante") : "Sem Nome";
                String curso = rs.getString("curso");
                String item = rs.getString("item") != null ? rs.getString("item") : "Item Sem Nome";
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
                } else {
                    statusExtenso = "Pendente";
                }

                lista.add(new SolicitacaoMock(id, data, solicitante, curso, item, quantidade, justificativa, statusExtenso, tipo));
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