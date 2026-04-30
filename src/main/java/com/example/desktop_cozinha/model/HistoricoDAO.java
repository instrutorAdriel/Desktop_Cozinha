package com.example.desktop_cozinha.model;
import com.example.desktop_cozinha.config.DatabaseConfig;
import javafx.fxml.FXML;

//import java.awt.*;
import java.sql.*;

public class HistoricoDAO {

    public List<ProdutoListaEstoque> lerTodos() {
        String sql = "SELECT nome, tipo, quantidade_atual, unidade_medida FROM estoque_geral";
        public List<ProdutoListaEstoque> filtrarProdutos(String nomeBusca) {
            String sql = "SELECT nome, tipo, quantidade_atual, unidade_medida FROM estoque_geral WHERE nome LIKE ?";
            List<ProdutoListaEstoque> itens = new ArrayList<>();

            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {
                PreparedStatement stmt = conn.prepareStatement(sql)) {

                    stmt.setString(1, "%" + nomeBusca + "%");

                    while (rs.next()) {
                        try (ResultSet rs = stmt.executeQuery()) {

                            ProdutoListaEstoque p = new ProdutoListaEstoque(
                                    rs.getString("nome"),
                                    rs.getString("tipo"),
                                    rs.getString("quantidade_atual"),
                                    rs.getString("unidade_medida")
                            );
                            while (rs.next()) {

                                itens.add(p);
                                ProdutoListaEstoque p = new ProdutoListaEstoque(
                                        rs.getString("nome"),
                                        rs.getString("tipo"),
                                        rs.getString("quantidade_atual"),
                                        rs.getString("unidade_medida")
                                );
                                itens.add(p);
                            }
                        }


                    }

    }
