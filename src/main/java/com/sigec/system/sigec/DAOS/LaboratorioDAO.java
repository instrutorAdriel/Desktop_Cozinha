package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class LaboratorioDAO {

    public static List<String> listarNomesLaboratoriosAtivos() {
        List<String> laboratorios = new ArrayList<>();
        // Consulta apenas laboratórios com situação 'A' (Ativos)
        String sql = "SELECT nome_laboratorio FROM laboratorio WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                laboratorios.add(rs.getString("nome_laboratorio"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar laboratórios: " + e.getMessage());
        }
        return laboratorios;
    }


    public static java.util.Map<String, Integer> listarLaboratoriosAtivosMap() {
        java.util.Map<String, Integer> laboratorios = new java.util.HashMap<>();
        String sql = "SELECT id_laboratorio, nome_laboratorio FROM laboratorio WHERE situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                laboratorios.put(rs.getString("nome_laboratorio"), rs.getInt("id_laboratorio"));
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar laboratórios (Map): " + e.getMessage());
        }
        return laboratorios;
    }
}