package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CursoDAO {
    public static List<String> listarNomesCursos() {
        List<String> cursos = new ArrayList<>();
        cursos.add("Todos");


        String sql = "SELECT nome_curso FROM curso WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cursos.add(rs.getString("nome_curso"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar cursos: " + e.getMessage());
        }
        return cursos;
    }
}