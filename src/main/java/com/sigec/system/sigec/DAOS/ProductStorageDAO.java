package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductStorageDAO {

    public List<ListaEstoque> buscarTodosProdutosEstoque() {
        return buscarProdutosPorFiltro("", "Todas", "Todos");
    }

    public List<ListaEstoque> buscarProdutosPorFiltro(String termo, String unidade, String curso) {
        List<ListaEstoque> lista = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("SELECT i.id_insumo, p.id_produto, p.nome_produto, c.nome_categoria AS tipo_produto, ");
        sql.append("i.lote, i.quantidade, COALESCE(i.unidade_medida, p.unidade_m, 'un') AS unidade ");
        sql.append("FROM insumo i ");
        sql.append("JOIN produto p ON i.id_produto = p.id_produto ");
        sql.append("LEFT JOIN categoria c ON p.id_categoria = c.id_categoria ");
        sql.append("LEFT JOIN unidade u ON i.id_unidade = u.id_unidade ");
        sql.append("WHERE p.situacao = 'A' AND i.cancelado = 'N' ");

        List<Object> parametros = new ArrayList<>();

        if (termo != null && !termo.trim().isEmpty()) {
            sql.append("AND (LOWER(p.nome_produto) LIKE LOWER(?) OR LOWER(COALESCE(i.lote, '')) LIKE LOWER(?)) ");
            parametros.add("%" + termo.trim() + "%");
            parametros.add("%" + termo.trim() + "%");
        }

        if (unidade != null && !unidade.equals("Todas") && !unidade.trim().isEmpty()) {
            sql.append("AND TRIM(u.nome_unidade) = TRIM(?) ");
            parametros.add(unidade.trim());
        } else {
            List<String> unidadesUsuario = UnidadeDAO.listarUnidadesDoUsuarioLogado(false);
            if (!unidadesUsuario.isEmpty()) {
                sql.append("AND TRIM(u.nome_unidade) IN (");
                for (int k = 0; k < unidadesUsuario.size(); k++) {
                    sql.append(k == 0 ? "TRIM(?)" : ", TRIM(?)");
                    parametros.add(unidadesUsuario.get(k).trim());
                }
                sql.append(") ");
            }
        }

        if (curso != null && !curso.equals("Todos") && !curso.trim().isEmpty()) {
            sql.append("AND EXISTS ( ");
            sql.append("    SELECT 1 FROM insumo_ficha inf ");
            sql.append("    JOIN curso_ficha cf ON inf.id_ficha = cf.id_ficha ");
            sql.append("    JOIN curso cu ON cu.id_curso = cf.id_curso ");
            sql.append("    WHERE inf.id_produto = p.id_produto ");
            sql.append("      AND cu.situacao = 'A' ");
            sql.append("      AND TRIM(cu.nome_curso) = TRIM(?) ");
            sql.append(") ");
            parametros.add(curso.trim());
        }

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int j = 0; j < parametros.size(); j++) {
                stmt.setObject(j + 1, parametros.get(j));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ListaEstoque produto = new ListaEstoque(
                            rs.getInt("id_insumo"),
                            rs.getInt("id_produto"),
                            rs.getString("nome_produto"),
                            rs.getString("lote") != null ? rs.getString("lote") : "S/L",
                            rs.getString("tipo_produto") != null ? rs.getString("tipo_produto") : "Sem Categoria",
                            rs.getDouble("quantidade"),
                            rs.getString("unidade")
                    );
                    lista.add(produto);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao filtrar lista de estoque: " + e.getMessage());
        }
        return lista;
    }

    public boolean excluirInsumo(int idInsumo, int idProduto) {
        Connection conn = null;
        try {
            conn = ConfigDataBase.getConnection();
            conn.setAutoCommit(false);

            String sqlCancelInsumo = "UPDATE insumo SET cancelado = 'S' WHERE id_insumo = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlCancelInsumo)) {
                stmt.setInt(1, idInsumo);
                stmt.executeUpdate();
            }

            String sqlCheckAtivos = "SELECT COUNT(*) FROM insumo WHERE id_produto = ? AND cancelado = 'N'";
            int lotesAtivos = 0;
            try (PreparedStatement stmtCheck = conn.prepareStatement(sqlCheckAtivos)) {
                stmtCheck.setInt(1, idProduto);
                try (ResultSet rs = stmtCheck.executeQuery()) {
                    if (rs.next()) {
                        lotesAtivos = rs.getInt(1);
                    }
                }
            }

            if (lotesAtivos == 0) {
                String sqlInativarProduto = "UPDATE produto SET situacao = 'I' WHERE id_produto = ?";
                try (PreparedStatement stmtProd = conn.prepareStatement(sqlInativarProduto)) {
                    stmtProd.setInt(1, idProduto);
                    stmtProd.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao excluir insumo/produto: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ignored) {}
        }
    }

    public boolean excluirProduto(int idProduto) {
        String sqlInsumo = "UPDATE insumo SET cancelado = 'S' WHERE id_produto = ?";
        String sqlProduto = "UPDATE produto SET situacao = 'I' WHERE id_produto = ?";
        try (Connection conn = ConfigDataBase.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt1 = conn.prepareStatement(sqlInsumo);
                 PreparedStatement stmt2 = conn.prepareStatement(sqlProduto)) {
                stmt1.setInt(1, idProduto);
                stmt1.executeUpdate();
                stmt2.setInt(1, idProduto);
                int afetados = stmt2.executeUpdate();
                conn.commit();
                return afetados > 0;
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            System.err.println("Erro ao excluir/zerar produto: " + e.getMessage());
            return false;
        }
    }

    public boolean verificarProdutoExisteAtivo(String nomeProduto) {
        String sql = "SELECT COUNT(*) FROM produto WHERE LOWER(nome_produto) = LOWER(?) AND situacao = 'A'";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeProduto.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar duplicidade: " + e.getMessage());
        }
        return false;
    }

    public boolean verificarLoteExisteAtivo(String nomeProduto, String lote, String nomeUnidade) {
        String sql = "SELECT COUNT(*) FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN unidade u ON i.id_unidade = u.id_unidade " +
                "WHERE LOWER(TRIM(p.nome_produto)) = LOWER(TRIM(?)) " +
                "AND LOWER(TRIM(COALESCE(i.lote, ''))) = LOWER(TRIM(?)) " +
                "AND TRIM(u.nome_unidade) = TRIM(?) " +
                "AND i.cancelado = 'N' AND p.situacao = 'A'";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeProduto != null ? nomeProduto.trim() : "");
            stmt.setString(2, lote != null ? lote.trim() : "");
            stmt.setString(3, nomeUnidade != null ? nomeUnidade.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar duplicidade de lote: " + e.getMessage());
        }
        return false;
    }

    private int obterOuCriarCategoria(Connection conn, String nomeCategoria) throws Exception {
        String sqlSelect = "SELECT id_categoria FROM categoria WHERE nome_categoria = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setString(1, nomeCategoria);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria");
                }
            }
        }

        String sqlInsert = "INSERT INTO categoria (nome_categoria) VALUES (?)";
        try (PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
            stmtInsert.setString(1, nomeCategoria);
            stmtInsert.executeUpdate();
            try (ResultSet rsKeys = stmtInsert.getGeneratedKeys()) {
                if (rsKeys.next()) {
                    return rsKeys.getInt(1);
                }
            }
        }
        return 1;
    }

    public boolean adicionarProduto(String nome, String nomeCategoria, double quantidade, double estoqueMinimo,
                                    String unidadeMedida, String lote, LocalDate dataValidade,
                                    String nomeUnidade) {
        return adicionarProduto(nome, nomeCategoria, quantidade, estoqueMinimo,
                unidadeMedida, lote, dataValidade, nomeUnidade, null);
    }

    public boolean adicionarProduto(String nome, String nomeCategoria, double quantidade, double estoqueMinimo,
                                    String unidadeMedida, String lote, LocalDate dataValidade,
                                    String nomeUnidade, Integer idFicha) {
        Connection conn = null;
        try {
            conn = ConfigDataBase.getConnection();
            conn.setAutoCommit(false);

            int idCategoria = obterOuCriarCategoria(conn, nomeCategoria);
            int idUnidade = obterIdUnidade(conn, nomeUnidade);
            int idProduto = -1;

            String sqlBuscaProd = "SELECT id_produto FROM produto WHERE LOWER(TRIM(nome_produto)) = LOWER(TRIM(?)) LIMIT 1";
            try (PreparedStatement stmtBusca = conn.prepareStatement(sqlBuscaProd)) {
                stmtBusca.setString(1, nome.trim());
                try (ResultSet rs = stmtBusca.executeQuery()) {
                    if (rs.next()) {
                        idProduto = rs.getInt("id_produto");
                    }
                }
            }

            if (idProduto > 0) {
                String sqlReativar = "UPDATE produto SET situacao = 'A', unidade_m = ?, id_categoria = ? WHERE id_produto = ?";
                try (PreparedStatement stmtUpd = conn.prepareStatement(sqlReativar)) {
                    stmtUpd.setString(1, unidadeMedida);
                    stmtUpd.setInt(2, idCategoria);
                    stmtUpd.setInt(3, idProduto);
                    stmtUpd.executeUpdate();
                }
            } else {
                String sqlProduto = "INSERT INTO produto (nome_produto, unidade_m, situacao, id_categoria) VALUES (?, ?, 'A', ?)";
                try (PreparedStatement stmt = conn.prepareStatement(sqlProduto, Statement.RETURN_GENERATED_KEYS)) {
                    stmt.setString(1, nome.trim());
                    stmt.setString(2, unidadeMedida);
                    stmt.setInt(3, idCategoria);
                    stmt.executeUpdate();
                    try (ResultSet rs = stmt.getGeneratedKeys()) {
                        if (!rs.next()) throw new Exception("ID do produto não gerado.");
                        idProduto = rs.getInt(1);
                    }
                }
            }

            String sqlInsumo = "INSERT INTO insumo (quantidade, quantidade_minima, unidade_medida, cancelado, " +
                    "id_produto, id_unidade, data_validade, lote) " +
                    "VALUES (?, ?, ?, 'N', ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlInsumo)) {
                stmt.setInt(1, (int) Math.round(quantidade));
                stmt.setInt(2, (int) Math.round(estoqueMinimo));
                stmt.setString(3, unidadeMedida);
                stmt.setInt(4, idProduto);
                stmt.setInt(5, idUnidade);
                if (dataValidade != null) {
                    stmt.setDate(6, java.sql.Date.valueOf(dataValidade));
                } else {
                    stmt.setNull(6, java.sql.Types.DATE);
                }
                stmt.setString(7, lote == null || lote.isBlank() ? null : lote.trim());
                stmt.executeUpdate();
            }

            if (idFicha != null && idFicha > 0) {
                String sqlVerificaFicha = "SELECT COUNT(*) FROM insumo_ficha WHERE id_ficha = ? AND id_produto = ?";
                boolean jaExiste = false;
                try (PreparedStatement stmtVer = conn.prepareStatement(sqlVerificaFicha)) {
                    stmtVer.setInt(1, idFicha);
                    stmtVer.setInt(2, idProduto);
                    try (ResultSet rs = stmtVer.executeQuery()) {
                        if (rs.next()) {
                            jaExiste = rs.getInt(1) > 0;
                        }
                    }
                }

                if (!jaExiste) {
                    String sqlInsereFicha = "INSERT INTO insumo_ficha (id_ficha, id_produto, quantidade, unidade_medida) " +
                            "VALUES (?, ?, ?, ?)";
                    try (PreparedStatement stmtIns = conn.prepareStatement(sqlInsereFicha)) {
                        stmtIns.setInt(1, idFicha);
                        stmtIns.setInt(2, idProduto);
                        stmtIns.setInt(3, (int) Math.round(quantidade));
                        stmtIns.setString(4, unidadeMedida);
                        stmtIns.executeUpdate();
                    }
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ignored) {}
        }
    }

    public boolean atualizarProduto(int idProduto, int idInsumo, String nome, String nomeCategoria, double quantidade,
                                    double estoqueMinimo, String unidadeMedida, String lote,
                                    LocalDate dataValidade, String nomeUnidade) {
        Connection conn = null;
        try {
            conn = ConfigDataBase.getConnection();
            conn.setAutoCommit(false);

            int idCategoria = obterOuCriarCategoria(conn, nomeCategoria);
            int idUnidade = obterIdUnidade(conn, nomeUnidade);

            String sqlProduto = "UPDATE produto SET nome_produto = ?, unidade_m = ?, id_categoria = ? WHERE id_produto = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlProduto)) {
                stmt.setString(1, nome.trim());
                stmt.setString(2, unidadeMedida);
                stmt.setInt(3, idCategoria);
                stmt.setInt(4, idProduto);
                if (stmt.executeUpdate() == 0) throw new Exception("Produto não encontrado.");
            }

            String sqlUpdate = "UPDATE insumo SET quantidade = ?, quantidade_minima = ?, unidade_medida = ?, " +
                    "id_unidade = ?, data_validade = ?, lote = ? " +
                    "WHERE id_insumo = ? AND cancelado = 'N'";
            int linhas;
            try (PreparedStatement stmt = conn.prepareStatement(sqlUpdate)) {
                preencherInsumo(stmt, quantidade, estoqueMinimo, unidadeMedida, idUnidade, dataValidade, lote);
                stmt.setInt(7, idInsumo);
                linhas = stmt.executeUpdate();
            }

            if (linhas == 0) {
                String sqlInsert = "INSERT INTO insumo (quantidade, quantidade_minima, unidade_medida, id_unidade, " +
                        "data_validade, lote, id_produto, cancelado) VALUES (?, ?, ?, ?, ?, ?, ?, 'N')";
                try (PreparedStatement stmt = conn.prepareStatement(sqlInsert)) {
                    preencherInsumo(stmt, quantidade, estoqueMinimo, unidadeMedida, idUnidade, dataValidade, lote);
                    stmt.setInt(7, idProduto);
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (Exception ignored) {}
        }
    }

    private void preencherInsumo(PreparedStatement stmt, double quantidade, double estoqueMinimo, String unidadeMedida,
                                 int idUnidade, LocalDate dataValidade, String lote) throws Exception {
        stmt.setInt(1, (int) Math.round(quantidade));
        stmt.setInt(2, (int) Math.round(estoqueMinimo));
        stmt.setString(3, unidadeMedida);
        stmt.setInt(4, idUnidade);
        if (dataValidade != null) stmt.setDate(5, java.sql.Date.valueOf(dataValidade));
        else stmt.setNull(5, java.sql.Types.DATE);
        stmt.setString(6, lote == null || lote.isBlank() ? null : lote.trim());
    }

    private int obterIdUnidade(Connection conn, String nomeUnidade) throws Exception {
        String sql = "SELECT id_unidade FROM unidade WHERE TRIM(nome_unidade) = TRIM(?) AND situacao = 'A'";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeUnidade != null ? nomeUnidade.trim() : "");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("id_unidade");
            }
        }
        throw new Exception("Unidade não encontrada: " + nomeUnidade);
    }

    public static class DetalhesInsumo {
        public double estoqueMinimo;
        public String lote;
        public LocalDate dataValidade;
        public String nomeUnidade;
        public double quantidade;
    }

    public DetalhesInsumo buscarDetalhesInsumo(int idInsumo) {
        String sql = "SELECT i.quantidade, i.quantidade_minima, i.lote, i.data_validade, u.nome_unidade " +
                "FROM insumo i LEFT JOIN unidade u ON i.id_unidade = u.id_unidade " +
                "WHERE i.id_insumo = ? AND i.cancelado = 'N' ";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idInsumo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    DetalhesInsumo d = new DetalhesInsumo();
                    d.estoqueMinimo = rs.getDouble("quantidade_minima");
                    d.lote = rs.getString("lote");
                    java.sql.Date data = rs.getDate("data_validade");
                    d.dataValidade = data != null ? data.toLocalDate() : null;
                    d.nomeUnidade = rs.getString("nome_unidade") != null ? rs.getString("nome_unidade").trim() : null;
                    d.quantidade = rs.getDouble("quantidade");
                    return d;
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar detalhes do insumo: " + e.getMessage());
        }
        return null;
    }

    public List<ListaEstoque> buscarInsumosParaAulas(LocalDate inicio, LocalDate fim) {
        List<ListaEstoque> lista = new ArrayList<>();

        String sql = "SELECT p.nome_produto, p.unidade_m, SUM(inf.quantidade) AS quantidade_total " +
                "FROM turma_ficha tf " +
                "JOIN insumo_ficha inf ON tf.id_ficha = inf.id_ficha " +
                "JOIN produto p ON inf.id_produto = p.id_produto " +
                "WHERE tf.data BETWEEN ? AND ? " +
                "GROUP BY p.nome_produto, p.unidade_m";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, java.sql.Date.valueOf(inicio));
            stmt.setDate(2, java.sql.Date.valueOf(fim));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ListaEstoque item = new ListaEstoque(
                            0,
                            rs.getString("nome_produto"),
                            "Para Aulas",
                            rs.getDouble("quantidade_total"),
                            rs.getString("unidade_m")
                    );
                    lista.add(item);
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar insumos para aulas: " + e.getMessage());
        }
        return lista;
    }
}