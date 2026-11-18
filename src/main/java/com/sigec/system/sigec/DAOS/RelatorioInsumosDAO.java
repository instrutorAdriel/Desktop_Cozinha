package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        sql.append("       p.unidade_m AS unidade_produto, ");
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

        sql.append("GROUP BY p.id_produto, p.nome_produto, p.unidade_m, cat.nome_categoria, unidade ");
        // Ordena por categoria (as sem categoria por último) e depois pelo nome
        sql.append("ORDER BY (cat.nome_categoria IS NULL), cat.nome_categoria, p.nome_produto");

        List<InsumoNecessario> lista = new ArrayList<>();
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            // O SQL agrupa por produto + unidade da ficha. Aqui:
            //  1. cada linha é convertida para a unidade base (g, ml) e somada por produto;
            //  2. o total é exibido na unidade de compra do produto (produto.unidade_m).
            // LinkedHashMap mantém a ordem do SQL (categoria, nome).
            Map<String, Acumulado> porProduto = new LinkedHashMap<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idProduto = rs.getInt("id_produto");
                    String categoria = rs.getString("categoria");
                    String nome = rs.getString("nome_produto");
                    String unidadeProduto = normalizarUnidade(rs.getString("unidade_produto"));

                    Medida base = paraUnidadeBase(rs.getDouble("quantidade_total"), rs.getString("unidade"));
                    // Contagem (un, maço...): sem conversão possível, vale a unidade do produto
                    if (!ehMassaOuVolume(base.unidade()) && !ehMassaOuVolume(unidadeProduto)) {
                        base = new Medida(base.quantidade(), unidadeProduto);
                    }
                    Medida b = base;
                    porProduto.computeIfAbsent(idProduto + "|" + b.unidade(),
                            k -> new Acumulado(idProduto, categoria, nome, b.unidade(), unidadeProduto))
                            .quantidade += b.quantidade();
                }
            }
            for (Acumulado a : porProduto.values()) {
                Medida exibicao = naUnidadeDoProduto(a.quantidade, a.unidadeBase, a.unidadeProduto);
                lista.add(new InsumoNecessario(a.idProduto, a.categoria, a.nomeProduto,
                        exibicao.quantidade(), exibicao.unidade()));
            }
        }
        return lista;
    }

    /** Soma parcial de um produto numa unidade base. */
    private static final class Acumulado {
        final int idProduto;
        final String categoria;
        final String nomeProduto;
        final String unidadeBase;
        final String unidadeProduto;
        double quantidade;

        Acumulado(int idProduto, String categoria, String nomeProduto, String unidadeBase, String unidadeProduto) {
            this.idProduto = idProduto;
            this.categoria = categoria;
            this.nomeProduto = nomeProduto;
            this.unidadeBase = unidadeBase;
            this.unidadeProduto = unidadeProduto;
        }
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


    // Conversão de unidades
    //   Massa  -> base "g"  (mg, g, kg)
    //   Volume -> base "ml" (ml, L)
    //   Outras (un, maço...) não se convertem: valem como a unidade do produto.
    //   O total é exibido na unidade de compra do produto.

    /** Quantidade + unidade. */
    record Medida(double quantidade, String unidade) {
    }

    /** Converte para a unidade base da grandeza: 3 kg -> 3000 g; 2 L -> 2000 ml. */
    static Medida paraUnidadeBase(double quantidade, String unidade) {
        String u = normalizarUnidade(unidade);
        return switch (u) {
            case "kg" -> new Medida(quantidade * 1000, "g");
            case "mg" -> new Medida(quantidade / 1000, "g");
            case "L"  -> new Medida(quantidade * 1000, "ml");
            default   -> new Medida(quantidade, u);
        };
    }

    /**
     * Exibe o total na unidade de compra do produto: 500 g de um produto comprado em kg -> 0,5 kg.
     * Se o produto não tiver unidade compatível (ex.: ficha em g, produto em "un"),
     * cai na regra geral de {@link #paraExibicao}.
     */
    static Medida naUnidadeDoProduto(double quantidadeBase, String unidadeBase, String unidadeProduto) {
        Medida produtoEmBase = paraUnidadeBase(1, unidadeProduto); // ex.: 1 kg -> 1000 g
        if (unidadeBase.equals(produtoEmBase.unidade())) {
            return new Medida(quantidadeBase / produtoEmBase.quantidade(), unidadeProduto);
        }
        return paraExibicao(quantidadeBase, unidadeBase);
    }

    static boolean ehMassaOuVolume(String unidade) {
        String base = paraUnidadeBase(1, unidade).unidade();
        return "g".equals(base) || "ml".equals(base);
    }

    /** Regra geral de leitura: a partir de 1.000 g mostra kg; a partir de 1.000 ml mostra L. */
    static Medida paraExibicao(double quantidadeBase, String unidadeBase) {
        if ("g".equals(unidadeBase) && quantidadeBase >= 1000) {
            return new Medida(quantidadeBase / 1000, "kg");
        }
        if ("ml".equals(unidadeBase) && quantidadeBase >= 1000) {
            return new Medida(quantidadeBase / 1000, "L");
        }
        return new Medida(quantidadeBase, unidadeBase);
    }

    // Padroniza a grafia da unidade: kg, g, mg, ml, L, un...

    static String normalizarUnidade(String unidade) {
        if (unidade == null || unidade.isBlank()) {
            return "un";
        }
        String u = unidade.trim().toLowerCase();
        return switch (u) {
            case "l", "lt", "lts", "litro", "litros" -> "L";
            case "kg", "kgs", "quilo", "quilos" -> "kg";
            case "g", "gr", "grama", "gramas" -> "g";
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