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
}