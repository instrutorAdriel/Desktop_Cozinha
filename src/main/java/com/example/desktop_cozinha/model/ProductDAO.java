package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public static List<Relatorio> relatorioProduto (String nomeProduto, int quantidade_atual, String unidade_medida, String data_validade, int estoque_minimo) throws SQLException {
    String sql = "SELECT * FROM produtos WHERE nome, quantidade_atual, unidade_medida, data_validade, estoque_minimo";

        List<Relatorio> itens = new ArrayList<>();

    try(Connection connection = DatabaseConfig.getConnection();
    PreparedStatement stmt = connection.prepareStatement(sql)) {
        try(ResultSet rs = stmt.executeQuery()){

            while(rs.next()){

                if(data_validade>data_atual){
                    String validade = "Produto expirado";
                }

                Relatorio r = new Relatorio(
                        rs.getString(nomeProduto),
                        rs.getInt(quantidade_atual),
                        rs.getString(unidade_medida),
                        rs.getString(validade),
                        rs.getInt(estoque_minimo)


                );
                itens.add(r);

            }

        }




    }catch (SQLException e){
        System.out.println("Erro ao buscar email: " + e.getMessage());
    }
    return itens;

}


}
