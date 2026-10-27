package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtensilioDAO {

    public static List<Utensilio> listarUtensilios() {
        List<Utensilio> lista = new ArrayList<>();
        // Busca os utensílios ativos no banco
        String sql = "SELECT id_utensilio, nome_utensilio, quantidade, situacao FROM utensilio WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int qtd = rs.getInt("quantidade");

                Utensilio utensilio = new Utensilio(
                        rs.getInt("id_utensilio"),
                        rs.getString("nome_utensilio"),
                        "Estoque Central",
                        "Disponível",
                        qtd,
                        qtd
                );
                lista.add(utensilio);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Erro ao listar utensílios.");
        }
        return lista;
    }
}