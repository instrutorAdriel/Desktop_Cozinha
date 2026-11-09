package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UnidadeDAO {

    // Busca apenas as unidades vinculadas a um usuário específico (tabela usuario_unidade)
    public static List<String> listarUnidadesPorUsuario(int idUsuario) {
        List<String> unidades = new ArrayList<>();

        String sql = "SELECT u.nome_unidade FROM unidade u " +
                "INNER JOIN usuario_unidade uu ON u.id_unidade = uu.id_unidade " +
                "WHERE uu.id_usuario = ? AND u.situacao = 'A'";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                unidades.add(rs.getString("nome_unidade"));
            }

        } catch (Exception e) {
            System.err.println("Erro ao buscar unidades do usuário: " + e.getMessage());
            e.printStackTrace();
        }

        // Se o usuário não tiver vínculo específico cadastrado, retorna a lista padrão
        if (unidades.isEmpty()) {
            return listarNomesUnidadesSemTodas();
        }

        return unidades;
    }

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

    private static List<String> listarNomesUnidadesSemTodas() {
        List<String> unidades = new ArrayList<>();
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