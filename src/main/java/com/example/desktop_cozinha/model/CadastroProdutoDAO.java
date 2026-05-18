package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CadastroProdutoDAO {
    // CREATE

    public void cadastrarProduto(String nomeProduto, String tipoProduto, String qtdAtual, String unidadeDeMedida, String estoqueMinimo, Date dataValidade) throws SQLException, ClassNotFoundException {
        String sql = "INSERT INTO estoque_geral (nome,tipo,quantidade_atual,unidade_medida,estoque_minimo,data_validade) VALUES (?, ?, ?,?,?,?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nomeProduto);
            stmt.setString(2, tipoProduto);
            stmt.setString(3, qtdAtual);
            stmt.setString(4, unidadeDeMedida);
            stmt.setString(5, estoqueMinimo);
            if (dataValidade != null) {
                // Se tem data, salva a data normal
                stmt.setDate(6, dataValidade);
            } else {
                // Se não tem data (é null), avisa o banco para salvar como campo vazio (NULL)
                stmt.setNull(6, java.sql.Types.DATE);
            }
            stmt.executeUpdate();
            IO.println("Produto Cadastrado com sucesso!");
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao cadastrar produto !", e);
        }
    }
}
