package com.example.desktop_cozinha.model;

import com.example.desktop_cozinha.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AlertaProdutoDAO {
    public List<AlertaProduto> buscarProdutosEmAlerta() {
        List<AlertaProduto> alertas = new ArrayList<>();
        String sql = """
            SELECT 
                nome, 
                IFNULL(DATE_FORMAT(data_validade, '%d/%m/%Y'), 'Sem data') AS validade,
                CASE 
                    WHEN data_validade IS NOT NULL AND data_validade < CURDATE() THEN 'Vencido'
                    WHEN data_validade IS NOT NULL AND data_validade = CURDATE() THEN 'Vence Hoje!'
                    WHEN data_validade IS NOT NULL AND data_validade BETWEEN DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) 
                        THEN CONCAT('Vencendo em ', DATEDIFF(data_validade, CURDATE()), ' dias')
                    WHEN quantidade_atual <= estoque_minimo THEN 'Estoque Baixo'
                    ELSE 'OK'
                END AS status_alerta,
                CONCAT(TRUNCATE(quantidade_atual, 3), ' ', unidade_medida) AS quantidade_formatada
            FROM estoque_geral
            WHERE (tipo = 'PERECIVEL' AND data_validade <= DATE_ADD(CURDATE(), INTERVAL 7 DAY)) 
               OR (quantidade_atual <= estoque_minimo);
        """;
        try(Connection conect = DatabaseConfig.getConnection();
            PreparedStatement cmd = conect.prepareStatement(sql);
            ResultSet rs = cmd.executeQuery()
        ){
            while (rs.next()) {
                // Criamos o objeto lendo os ALIAS (apelidos) definidos no SELECT do SQL
                AlertaProduto alerta = new AlertaProduto(
                        rs.getString("nome"),               // nome original da coluna
                        rs.getString("validade"),           // apelido do IFNULL(DATE_FORMAT...)
                        rs.getString("status_alerta"),      // apelido do CASE WHEN
                        rs.getString("quantidade_formatada") // apelido do CONCAT(TRUNCATE...)
                );
                alertas.add(alerta);
            }
        } catch (SQLException e) {
            // Log simples de erro para monitoramento no console
            System.err.println("Erro crítico ao executar a busca de alertas no banco: " + e.getMessage());
            e.printStackTrace();
        }
        return alertas;
    }
}
