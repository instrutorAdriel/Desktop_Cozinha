package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoriaDAO {
    public static List<String> listarNomesCategorias() {
        List<String> categorias = new ArrayList<>();
        String sql = "SELECT nome_categoria FROM categoria ORDER BY nome_categoria";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(rs.getString("nome_categoria"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar categorias: " + e.getMessage());
        }
        return categorias;
    }
}