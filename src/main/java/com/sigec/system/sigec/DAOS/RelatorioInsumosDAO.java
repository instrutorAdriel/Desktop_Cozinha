package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas da tela de Calendário:
 *  - filtros em cascata (Unidade -> Curso -> Turma)
 *  - relatório de insumos necessários para as aulas agendadas no período.
 *
 * Caminho entre as tabelas:
 *   unidade <- laboratorio.id_unidade <- turma.id_laboratorio -> turma.id_curso -> curso
 *   agendamento -> ficha -> insumo_ficha -> produto
 */
public class RelatorioInsumosDAO {

    /** Item simples para os ChoiceBox (id + nome). O toString() é o texto exibido. */
    public record Opcao(int id, String nome) {
        @Override
        public String toString() {
            return nome;
        }
    }

    /** Uma linha do relatório: categoria, produto, quantidade somada e unidade de medida. */
    public record InsumoNecessario(int idProduto, String categoria, String nomeProduto,
                                   double quantidade, String unidade) {
    }

    public static final String SEM_CATEGORIA = "Sem categoria";


    //  Unidades (respeita as unidades vinculadas ao usuário logado)

    public List<Opcao> listarUnidades() throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id_unidade, TRIM(nome_unidade) AS nome FROM unidade WHERE situacao = 'A' ");
        List<Object> parametros = new ArrayList<>();


        List<String> unidadesUsuario = UnidadeDAO.listarUnidadesDoUsuarioLogado(false);
        if (!unidadesUsuario.isEmpty()) {
            sql.append("AND TRIM(nome_unidade) IN (");
            for (int i = 0; i < unidadesUsuario.size(); i++) {
                sql.append(i == 0 ? "?" : ", ?");
                parametros.add(unidadesUsuario.get(i).trim());
            }
            sql.append(") ");
        }
        sql.append("ORDER BY nome");

        return listarOpcoes(sql.toString(), parametros);
    }


    //  Cursos que têm turma ativa em algum laboratório da unidade

    public List<Opcao> listarCursosPorUnidade(int idUnidade) throws SQLException {
        String sql = "SELECT DISTINCT c.id_curso, c.nome_curso " +
                "FROM curso c " +
                "JOIN turma t       ON t.id_curso = c.id_curso AND t.situacao = 'A' " +
                "JOIN laboratorio l ON l.id_laboratorio = t.id_laboratorio " +
                "WHERE l.id_unidade = ? AND c.situacao = 'A' " +
                "ORDER BY c.nome_curso";
        return listarOpcoes(sql, List.of(idUnidade));
    }


    //  Turmas do curso dentro da unidade

    public List<Opcao> listarTurmasPorCursoEUnidade(int idCurso, int idUnidade) throws SQLException {
        String sql = "SELECT t.id_turma, t.nome_turma " +
                "FROM turma t " +
                "JOIN laboratorio l ON l.id_laboratorio = t.id_laboratorio " +
                "WHERE t.id_curso = ? AND l.id_unidade = ? AND t.situacao = 'A' " +
                "ORDER BY t.nome_turma";
        return listarOpcoes(sql, List.of(idCurso, idUnidade));
    }


    //  Relatório: soma dos insumos das fichas agendadas no período
    //    idCurso / idTurma = null  ->  "Todos"

    public List<InsumoNecessario> buscarInsumosNecessarios(LocalDate inicio, LocalDate fim, int idUnidade,
                                                           Integer idCurso, Integer idTurma) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id_produto, p.nome_produto, ");
        sql.append("       COALESCE(cat.nome_categoria, '" + SEM_CATEGORIA + "') AS categoria, ");
        sql.append("       LOWER(COALESCE(inf.unidade_medida, p.unidade_m)) AS unidade, ");
        sql.append("       SUM(inf.quantidade) AS quantidade_total ");
        sql.append("FROM agendamento a ");
        // O agendamento pode apontar para a ficha direto ou via turma_ficha
        sql.append("LEFT JOIN turma_ficha tf ON tf.id_turma_ficha = a.id_turma_ficha ");
        sql.append("JOIN ficha f          ON f.id_ficha = COALESCE(a.id_ficha, tf.id_ficha) ");
        // A turma vem do agendamento; se faltar, da turma_ficha; se faltar, da própria ficha
        sql.append("JOIN turma t          ON t.id_turma = COALESCE(a.id_turma, tf.id_turma, f.id_turma) ");
        sql.append("JOIN laboratorio l    ON l.id_laboratorio = t.id_laboratorio ");
        sql.append("JOIN insumo_ficha inf ON inf.id_ficha = f.id_ficha ");
        sql.append("JOIN produto p        ON p.id_produto = inf.id_produto ");
        sql.append("LEFT JOIN categoria cat ON cat.id_categoria = p.id_categoria ");
        sql.append("WHERE a.situacao = 'A' AND f.situacao = 'A' ");
        sql.append("  AND a.data BETWEEN ? AND ? ");
        sql.append("  AND l.id_unidade = ? ");

        List<Object> parametros = new ArrayList<>();
        parametros.add(java.sql.Date.valueOf(inicio));
        parametros.add(java.sql.Date.valueOf(fim));
        parametros.add(idUnidade);

        if (idCurso != null) {
            sql.append("  AND t.id_curso = ? ");
            parametros.add(idCurso);
        }
        if (idTurma != null) {
            sql.append("  AND t.id_turma = ? ");
            parametros.add(idTurma);
        }

        sql.append("GROUP BY p.id_produto, p.nome_produto, cat.nome_categoria, unidade ");
        // Ordena por categoria (as sem categoria por último) e depois pelo nome
        sql.append("ORDER BY (cat.nome_categoria IS NULL), cat.nome_categoria, p.nome_produto");

        List<InsumoNecessario> lista = new ArrayList<>();
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new InsumoNecessario(
                            rs.getInt("id_produto"),
                            rs.getString("categoria"),
                            rs.getString("nome_produto"),
                            rs.getDouble("quantidade_total"),
                            normalizarUnidade(rs.getString("unidade"))
                    ));
                }
            }
        }
        return lista;
    }


    //  Nome do usuário logado

    public String buscarNomeUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT nome_usuario FROM usuario WHERE id_usuario = ?";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? rs.getString(1).trim() : null;
            }
        }
    }


    // Padroniza a unidade de medida para exibição: kg, g, mg, ml, L, un...

    static String normalizarUnidade(String unidade) {
        if (unidade == null || unidade.isBlank()) {
            return "un";
        }
        String u = unidade.trim().toLowerCase();
        return switch (u) {
            case "l", "lt", "lts", "litro", "litros" -> "L";
            case "un", "und", "unid", "unidade", "unidades" -> "un";
            default -> u;
        };
    }


    //  executa um SELECT que devolve (id, nome)

    private List<Opcao> listarOpcoes(String sql, List<?> parametros) throws SQLException {
        List<Opcao> lista = new ArrayList<>();
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Opcao(rs.getInt(1), rs.getString(2).trim()));
                }
            }
        }
        return lista;
    }
}