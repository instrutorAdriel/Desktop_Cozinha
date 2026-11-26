package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


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

    /**
     * Uma linha da lista de compras. Os três números estão na mesma unidade (a de compra do produto).
     * aComprar = necessário + mínimo - estoque, nunca negativo.
     */
    public record ItemCompra(int idProduto, String codigoMxm, String categoria, String nomeProduto,
                             double necessario, double emEstoque, double aComprar, String unidade) {
        public boolean precisaComprar() {
            return aComprar > 0.0005;
        }
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
        List<InsumoNecessario> lista = new ArrayList<>();
        for (Acumulado a : somarNecessidade(inicio, fim, idUnidade, idCurso, idTurma).values()) {
            Medida exibicao = naUnidadeDoProduto(a.quantidade, a.unidadeBase, a.unidadeProduto);
            lista.add(new InsumoNecessario(a.idProduto, a.categoria, a.nomeProduto,
                    exibicao.quantidade(), exibicao.unidade()));
        }
        return lista;
    }

    /**
     * Soma o que as aulas do período pedem, por produto, já na unidade base (g, ml ou contagem).
     * Chave do mapa: "idProduto|unidadeBase". A ordem segue o SQL (categoria, nome).
     */
    private Map<String, Acumulado> somarNecessidade(LocalDate inicio, LocalDate fim, int idUnidade,
                                                    Integer idCurso, Integer idTurma) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT p.id_produto, p.codigo_mxm, p.nome_produto, ");
        sql.append("       COALESCE(cat.nome_categoria, '" + SEM_CATEGORIA + "') AS categoria, ");
        sql.append("       LOWER(COALESCE(inf.unidade_medida, p.unidade_m)) AS unidade, ");
        sql.append("       p.unidade_m AS unidade_produto, ");
        sql.append("       a.data AS data_aula, ");
        sql.append("       SUM(inf.quantidade) AS quantidade_total ");
        sql.append("FROM agendamento a ");
        sql.append("JOIN ficha f          ON f.id_ficha = a.id_ficha ");
        sql.append("JOIN turma t          ON t.id_turma = a.id_turma ");
        sql.append("JOIN laboratorio l    ON l.id_laboratorio = t.id_laboratorio ");
        sql.append("JOIN insumo_ficha inf ON inf.id_ficha = f.id_ficha ");
        sql.append("JOIN produto p        ON p.id_produto = inf.id_produto ");
        sql.append("LEFT JOIN categoria cat ON cat.id_categoria = p.id_categoria ");
        sql.append("WHERE a.situacao = 'A' AND f.situacao = 'A' ");
        // Aula concluída já teve o material retirado do estoque: não entra de novo na necessidade
        sql.append("  AND COALESCE(a.concluido, 'N') <> 'S' ");
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

        sql.append("GROUP BY p.id_produto, p.codigo_mxm, p.nome_produto, p.unidade_m, cat.nome_categoria, unidade, a.data ");
        // Ordena por categoria (as sem categoria por último), nome e data da aula
        sql.append("ORDER BY (cat.nome_categoria IS NULL), cat.nome_categoria, p.nome_produto, a.data");

        Map<String, Acumulado> porProduto = new LinkedHashMap<>();
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            // O SQL agrupa por produto + unidade da ficha. Aqui:
            //  1. cada linha é convertida para a unidade base (g, ml) e somada por produto;
            //  2. o total é exibido na unidade de compra do produto (produto.unidade_m).
            // LinkedHashMap mantém a ordem do SQL (categoria, nome).
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int idProduto = rs.getInt("id_produto");
                    String categoria = rs.getString("categoria");
                    String nome = rs.getString("nome_produto");
                    String codigoMxm = rs.getString("codigo_mxm");
                    String unidadeProduto = normalizarUnidade(rs.getString("unidade_produto"));

                    LocalDate dataAula = rs.getDate("data_aula").toLocalDate();

                    Medida b = paraBaseDoProduto(rs.getDouble("quantidade_total"), rs.getString("unidade"), unidadeProduto);
                    Acumulado acc = porProduto.computeIfAbsent(idProduto + "|" + b.unidade(),
                            k -> new Acumulado(idProduto, codigoMxm, categoria, nome, b.unidade(), unidadeProduto));
                    acc.quantidade += b.quantidade();
                    acc.porData.merge(dataAula, b.quantidade(), Double::sum);
                }
            }
        }
        return porProduto;
    }

    //  Lista de compras: necessário x estoque disponível na unidade
    //    Necessário = aulas ativas e ainda não concluídas do período.
    //    Cada lote só atende aulas que acontecem até a data de validade dele
    //    (lote sem validade atende qualquer aula). O consumo é FEFO: em cada aula,
    //    gasta primeiro o lote que vence antes.
    //    Em estoque = quanto dos lotes é aproveitável: o que as aulas usam
    //                 + a sobra que ainda está válida no fim do período.
    //    A comprar  = necessário + mínimo - em estoque, nunca negativo.
    //    Mínimo = maior quantidade_minima entre os lotes não cancelados do produto.

    public List<ItemCompra> buscarListaDeCompras(LocalDate inicio, LocalDate fim, int idUnidade,
                                                 Integer idCurso, Integer idTurma) throws SQLException {
        Map<String, Acumulado> necessidade = somarNecessidade(inicio, fim, idUnidade, idCurso, idTurma);
        List<ItemCompra> lista = new ArrayList<>();
        if (necessidade.isEmpty()) {
            return lista;
        }

        Map<String, EstoqueProduto> estoque = listarLotes(idUnidade);

        for (Map.Entry<String, Acumulado> e : necessidade.entrySet()) {
            Acumulado a = e.getValue();
            EstoqueProduto est = estoque.getOrDefault(e.getKey(), new EstoqueProduto());
            double aproveitavel = calcularAproveitavel(a, est, fim);
            double aComprarBase = Math.max(0, a.quantidade + est.minimo - aproveitavel);

            // Os três números na mesma unidade de exibição (a de compra do produto)
            Medida exibicao = naUnidadeDoProduto(a.quantidade, a.unidadeBase, a.unidadeProduto);
            double fator = paraUnidadeBase(1, exibicao.unidade()).quantidade(); // ex.: 1 kg = 1000 g
            lista.add(new ItemCompra(a.idProduto, a.codigoMxm, a.categoria, a.nomeProduto,
                    a.quantidade / fator, aproveitavel / fator, aComprarBase / fator, exibicao.unidade()));
        }
        return lista;
    }

    /**
     * Simula as aulas em ordem de data, consumindo os lotes por FEFO.
     * Devolve o total aproveitável: consumido nas aulas + sobra válida no fim do período.
     */
    private static double calcularAproveitavel(Acumulado a, EstoqueProduto est, LocalDate fim) {
        // Cópia dos saldos para não alterar o estoque original
        List<Lote> lotes = new ArrayList<>();
        for (Lote l : est.lotes) {
            lotes.add(new Lote(l.validade, l.saldo));
        }
        // Vence antes = usa antes; sem validade vai para o fim da fila
        lotes.sort(Comparator.comparing((Lote l) -> l.validade,
                Comparator.nullsLast(Comparator.naturalOrder())));

        double consumido = 0;
        for (Map.Entry<LocalDate, Double> aula : a.porData.entrySet()) {
            LocalDate dia = aula.getKey();
            double falta = aula.getValue();
            for (Lote l : lotes) {
                if (falta <= 0) break;
                if (l.saldo <= 0 || !l.validoEm(dia)) continue;
                double usa = Math.min(falta, l.saldo);
                l.saldo -= usa;
                falta -= usa;
                consumido += usa;
            }
        }

        double sobraValida = 0;
        for (Lote l : lotes) {
            if (l.saldo > 0 && l.validoEm(fim)) {
                sobraValida += l.saldo;
            }
        }
        return consumido + sobraValida;
    }

    /** Lotes não cancelados da unidade, por produto, na unidade base. */
    private Map<String, EstoqueProduto> listarLotes(int idUnidade) throws SQLException {
        String sql = "SELECT i.id_produto, " +
                "       LOWER(COALESCE(i.unidade_medida, p.unidade_m)) AS unidade, " +
                "       p.unidade_m AS unidade_produto, " +
                "       i.quantidade, " +
                "       COALESCE(i.quantidade_minima, 0) AS minimo, " +
                "       i.data_validade " +
                "FROM insumo i " +
                "JOIN produto p ON p.id_produto = i.id_produto " +
                "WHERE i.cancelado = 'N' AND i.id_unidade = ?";

        Map<String, EstoqueProduto> porProduto = new LinkedHashMap<>();
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUnidade);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String unidadeLote = rs.getString("unidade");
                    String unidadeProduto = normalizarUnidade(rs.getString("unidade_produto"));
                    Medida qtd = paraBaseDoProduto(rs.getDouble("quantidade"), unidadeLote, unidadeProduto);
                    Medida min = paraBaseDoProduto(rs.getDouble("minimo"), unidadeLote, unidadeProduto);
                    java.sql.Date validade = rs.getDate("data_validade");

                    EstoqueProduto ep = porProduto.computeIfAbsent(
                            rs.getInt("id_produto") + "|" + qtd.unidade(), k -> new EstoqueProduto());
                    ep.minimo = Math.max(ep.minimo, min.quantidade());
                    if (qtd.quantidade() > 0) {
                        ep.lotes.add(new Lote(validade == null ? null : validade.toLocalDate(), qtd.quantidade()));
                    }
                }
            }
        }
        return porProduto;
    }

    /** Um lote com saldo na unidade base. Validade null = não vence. */
    private static final class Lote {
        final LocalDate validade;
        double saldo;

        Lote(LocalDate validade, double saldo) {
            this.validade = validade;
            this.saldo = saldo;
        }

        boolean validoEm(LocalDate dia) {
            return validade == null || !validade.isBefore(dia);
        }
    }

    /** Lotes e estoque mínimo de um produto na unidade. */
    private static final class EstoqueProduto {
        final List<Lote> lotes = new ArrayList<>();
        double minimo;
    }

    /** Soma parcial de um produto numa unidade base. */
    private static final class Acumulado {
        final int idProduto;
        final String codigoMxm;
        final String categoria;
        final String nomeProduto;
        final String unidadeBase;
        final String unidadeProduto;
        double quantidade;
        /** Necessidade por data de aula (unidade base), em ordem cronológica. */
        final TreeMap<LocalDate, Double> porData = new TreeMap<>();

        Acumulado(int idProduto, String codigoMxm, String categoria, String nomeProduto,
                  String unidadeBase, String unidadeProduto) {
            this.idProduto = idProduto;
            this.codigoMxm = codigoMxm;
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

    /**
     * Converte para a base usada nas somas. Massa/volume: g ou ml.
     * Contagem (un, maço...): sem conversão possível, vale a unidade do produto.
     */
    static Medida paraBaseDoProduto(double quantidade, String unidade, String unidadeProduto) {
        Medida base = paraUnidadeBase(quantidade, unidade);
        if (!ehMassaOuVolume(base.unidade()) && !ehMassaOuVolume(unidadeProduto)) {
            return new Medida(base.quantidade(), unidadeProduto);
        }
        return base;
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