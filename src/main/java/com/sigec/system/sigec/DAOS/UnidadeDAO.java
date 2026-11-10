package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import com.sigec.system.sigec.Services.SessaoService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UnidadeDAO {

    /**
     * Utilizado nos filtros de listagem (adiciona a opção "Todas" se houver mais de uma unidade).
     */
    public static List<String> listarNomesUnidades() {
        return listarUnidadesDoUsuarioLogado(true);
    }


    public static List<String> listarUnidadesParaCadastro() {
        return listarUnidadesDoUsuarioLogado(false);
    }


    public static List<String> listarUnidadesDoUsuarioLogado(boolean incluirOpcaoTodas) {
        List<String> unidades = new ArrayList<>();
        User usuarioLogado = SessaoService.getUsuarioLogado();

        int idUsuario = (usuarioLogado != null && usuarioLogado.getIdUsuario() > 0)
                ? usuarioLogado.getIdUsuario()
                : 1; // Fallback de segurança para testes se a sessão estiver nula

        String sql = "SELECT DISTINCT TRIM(u.nome_unidade) AS nome_unidade " +
                "FROM unidade u " +
                "INNER JOIN usuario_unidade uu ON uu.id_unidade = u.id_unidade " +
                "WHERE uu.id_usuario = ? AND u.situacao = 'A' " +
                "ORDER BY nome_unidade";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unidades.add(rs.getString("nome_unidade"));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao listar unidades do utilizador via usuario_unidade: " + e.getMessage());
        }


        if (unidades.isEmpty()) {
            String sqlFallback = "SELECT TRIM(nome_unidade) AS nome_unidade FROM unidade WHERE situacao = 'A' ORDER BY nome_unidade";
            try (Connection conn = ConfigDataBase.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sqlFallback);
                 ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    unidades.add(rs.getString("nome_unidade"));
                }
            } catch (Exception e) {
                System.err.println("Erro no fallback de unidades: " + e.getMessage());
            }
        }

        if (incluirOpcaoTodas && unidades.size() > 1) {
            unidades.add(0, "Todas");
        }

        return unidades;
    }
}