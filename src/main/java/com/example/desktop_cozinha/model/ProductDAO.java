package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public static List<Notificacao> relatorioProduto () throws SQLException {
    String sql = "SELECT id, nome, tipo, quantidade_atual, unidade_medida, estoque_minimo FROM estoque_geral";

        List<Notificacao> itens = new ArrayList<>();


    try(Connection connection = DatabaseConfig.getConnection();
    PreparedStatement stmt = connection.prepareStatement(sql)) {
        try(ResultSet rs = stmt.executeQuery()){

            while(rs.next()){

                Notificacao r = new Notificacao(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getString("tipo"),
                        rs.getInt("quantidade_atual"),
                        rs.getString("unidade_medida"),
                        rs.getString("data_validade"),
                        rs.getInt("estoque_minimo")

                );
                if(r.getEstoque_minimo() <= r.getQuantidade_atual()) {
                    itens.add(r);
                }

            }

        }

    }catch (SQLException e){
        System.out.println("Erro ao buscar email: " + e.getMessage());
    }
    return itens;

}
}
