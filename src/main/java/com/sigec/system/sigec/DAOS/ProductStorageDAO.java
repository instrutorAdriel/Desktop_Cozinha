package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProductStorageDAO {

    public List<ListaEstoque> buscarTodosProdutosEstoque() {
        List<ListaEstoque> lista = new ArrayList<>();
        // Query  para buscar e somar a quantidade da tabela insumo
        String sql = "SELECT p.id_produto, p.nome_produto, c.nome_categoria AS tipo_produto, " +
                "COALESCE(SUM(i.quantidade), 0) AS quantidade, p.unidade " +
                "FROM produto p " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "LEFT JOIN insumo i ON p.id_produto = i.id_produto AND i.cancelado = 'N' " +
                "WHERE p.situacao = 'A' " +
                "GROUP BY p.id_produto, p.nome_produto, c.nome_categoria, p.unidade";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ListaEstoque produto = new ListaEstoque(
                        rs.getInt("id_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("tipo_produto") != null ? rs.getString("tipo_produto") : "Sem Categoria",
                        rs.getDouble("quantidade"), // Agora vem do SUM(i.quantidade)
                        rs.getString("unidade")
                );
                lista.add(produto);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar lista de estoque: " + e.getMessage());
        }
        return lista;
    }

    public boolean excluirProduto(int idProduto) {
        String sql = "UPDATE produto SET situacao = 'I', quantidade = 0 WHERE id_produto = ?";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idProduto);
            return stmt.executeUpdate() > 0;
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

    // =========================================================================
    // INTELIGÊNCIA: Acha a categoria no banco. Se não existir, cria na hora!
    // =========================================================================
    private int obterOuCriarCategoria(Connection conn, String nomeCategoria) throws Exception {
        String sqlSelect = "SELECT id_categoria FROM categoria WHERE nome_categoria = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sqlSelect)) {
            stmt.setString(1, nomeCategoria);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_categoria"); // Achou a categoria, retorna o ID
                }
            }
        }

        // Se não achou, insere na tabela 'categoria' e pega o ID gerado
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
        return 1; // Fallback de emergência
    }

    public boolean adicionarProduto(String nome, String nomeCategoria, double quantidade, String unidade) {
        try (Connection conn = ConfigDataBase.getConnection()) {

            // 1. Usa o método inteligente para ter certeza de que a categoria existe
            int idCategoria = obterOuCriarCategoria(conn, nomeCategoria);

            // 2. Insere o produto usando o ID correto
            String sql = "INSERT INTO produto (nome_produto, id_categoria, quantidade, unidade, situacao) VALUES (?, ?, ?, ?, 'A')";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setInt(2, idCategoria);
                stmt.setDouble(3, quantidade);
                stmt.setString(4, unidade);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizarProduto(int idProduto, String nome, String nomeCategoria, double quantidade, String unidade) {
        try (Connection conn = ConfigDataBase.getConnection()) {

            // 1. Acha ou cria a categoria nova
            int idCategoria = obterOuCriarCategoria(conn, nomeCategoria);

            // 2. Salva a atualização
            String sql = "UPDATE produto SET nome_produto = ?, id_categoria = ?, quantidade = ?, unidade = ? WHERE id_produto = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setInt(2, idCategoria);
                stmt.setDouble(3, quantidade);
                stmt.setString(4, unidade);
                stmt.setInt(5, idProduto);
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    }
}