package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class CategoriaUtensilioDAO {

    public static Map<String, Integer> listarCategoriasAtivas() {
        Map<String, Integer> categorias = new HashMap<>();
        String sql = "SELECT id_categoria_utensilio, nome_categoria FROM categoria_utensilio WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.put(rs.getString("nome_categoria"), rs.getInt("id_categoria_utensilio"));
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar categorias de utensílios: " + e.getMessage());
        }
        return categorias;
    }
}