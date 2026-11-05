package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Retorna o mapa (nome_unidade -> id_unidade) apenas das unidades ativas
     * nas quais o usuário informado está vinculado através da tabela usuario_unidade.
     */
    public static Map<String, Integer> listarUnidadesPorUsuarioMap(int idUsuario) {
        Map<String, Integer> unidades = new LinkedHashMap<>();

        String sql = "SELECT u.id_unidade, u.nome_unidade " +
                     "FROM unidade u " +
                     "INNER JOIN usuario_unidade uu ON u.id_unidade = uu.id_unidade " +
                     "WHERE uu.id_usuario = ? AND u.situacao = 'A' " +
                     "ORDER BY u.nome_unidade ASC";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String nome = rs.getString("nome_unidade");
                    if (nome != null) {
                        unidades.put(nome.trim(), rs.getInt("id_unidade"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao buscar unidades vinculadas ao usuário (" + idUsuario + "): " + e.getMessage());
        }
        return unidades;
    }
}
