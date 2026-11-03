package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UnidadeDAO {
    public static List<String> listarNomesUnidades() {
        List<String> unidades = new ArrayList<>();
        unidades.add("Todas"); // Opção padrão para o filtro


        String sql = "SELECT nome_unidade FROM unidade WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                unidades.add(rs.getString("nome_unidade"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar unidades: " + e.getMessage());
        }
        return unidades;
    }
}