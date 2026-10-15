package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.ListaEstoque;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acesso a Dados (DAO) para produtos e controle de estoque.
 */
public class ProductStorageDAO {

    public List<ListaEstoque> buscarTodosProdutosEstoque() {
        List<ListaEstoque> lista = new ArrayList<>();
        String sql = "SELECT p.id_produto, p.nome_produto, c.nome_categoria AS tipo_produto, p.quantidade, p.unidade " +
                "FROM produto p " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE p.situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ListaEstoque produto = new ListaEstoque(
                        rs.getInt("id_produto"),
                        rs.getString("nome_produto"),
                        rs.getString("tipo_produto") != null ? rs.getString("tipo_produto") : "Sem Categoria",
                        rs.getDouble("quantidade"),
                        rs.getString("Unidade")
                );
                lista.add(produto);
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar lista de estoque: " + e.getMessage());
        }
        return lista;
    }

    // -------------------------------------------------------------------------
    // EXCLUIR PRODUTO (Exclusão Lógica: Muda situação para Inativo)
    // -------------------------------------------------------------------------
    public boolean excluirProduto(int idProduto) {
        String sql = "UPDATE produto SET situacao = 'I', quantidade = 0 WHERE id_produto = ?";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idProduto);
            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erro ao excluir produto: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // ADICIONAR PRODUTO NOVO
    // -------------------------------------------------------------------------
    public boolean adicionarProduto(String nome, String nomeCategoria, double quantidade, String unidade) {
        // Usa um sub-select para achar o ID da categoria com base no nome selecionado no Dropdown
        String sql = "INSERT INTO produto (nome_produto, id_categoria, quantidade, unidade, situacao) " +
                "VALUES (?, (SELECT id_categoria FROM categoria WHERE nome_categoria = ? LIMIT 1), ?, ?, 'A')";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setString(2, nomeCategoria);
            stmt.setDouble(3, quantidade);
            stmt.setString(4, unidade);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // ATUALIZAR PRODUTO EXISTENTE
    // -------------------------------------------------------------------------
    public boolean atualizarProduto(int idProduto, String nome, String nomeCategoria, double quantidade, String unidade) {
        String sql = "UPDATE produto SET nome_produto = ?, " +
                "id_categoria = (SELECT id_categoria FROM categoria WHERE nome_categoria = ? LIMIT 1), " +
                "quantidade = ?, unidade = ? " +
                "WHERE id_produto = ?";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nome);
            stmt.setString(2, nomeCategoria);
            stmt.setDouble(3, quantidade);
            stmt.setString(4, unidade);
            stmt.setInt(5, idProduto);

            return stmt.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Erro ao atualizar produto: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------------------------
    // VERIFICAR SE O PRODUTO JÁ EXISTE (Evitar Duplicação)
    // -------------------------------------------------------------------------
    public boolean verificarProdutoExisteAtivo(String nomeProduto) {
        // Busca se existe algum produto com o mesmo nome (ignorando maiúsculas/minúsculas) que esteja ativo
        String sql = "SELECT COUNT(*) FROM produto WHERE LOWER(nome_produto) = LOWER(?) AND situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeProduto.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0; // Retorna true se a contagem for maior que zero
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao verificar duplicidade: " + e.getMessage());
        }
        return false; // Se der erro, assume que não existe para não travar o sistema inteiro
    }
}