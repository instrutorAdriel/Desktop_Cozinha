package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.AlertaEstoqueDTO;
import com.sigec.system.sigec.Constructors.Historico;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acesso a Dados (DAO) para o histórico e auditoria de movimentações.
 */
public class HistoryDAO {

    public List<Historico> listarHistorico() {
        // Implementação de busca de todas as movimentações registradas
        return new ArrayList<>();
    }

    public List<Historico> filtrarPorPeriodo(LocalDate inicio, LocalDate fim) {
        // Implementação de filtragem por intervalo de datas
        return new ArrayList<>();
    }

    public boolean registrarMovimentacao(Historico historico) {
        return historico != null;
    }



    // 5. Lista de Compras (Une Estoque Baixo e Estoque Zerado)
    public static List<HistoryDAO> listarListaDeCompras(int idUnidade) {
        List<HistoryDAO> lista = new ArrayList<>();

        // Puxando 'i.quantidade' e 'i.quantidade_minima' da tabela insumo
        String sql = "SELECT p.nome_produto, c.nome_categoria, i.quantidade, i.quantidade_minima, i.lote " +
                "FROM insumo i " +
                "JOIN produto p ON i.id_produto = p.id_produto " +
                "LEFT JOIN categoria c ON p.id_categoria = c.id_categoria " +
                "WHERE (i.quantidade <= 0 OR i.quantidade <= i.quantidade_minima) " +
                "AND i.cancelado = 'N' AND i.id_unidade = ?";
        IO.println(sql);
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUnidade);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    HistoryDAO alerta = new HistoryDAO();
                    alerta.setNomeProduto(rs.getString("nome_produto"));
                    alerta.setNomeCategoria(rs.getString("nome_categoria"));
                    alerta.setLote(rs.getString("lote"));

                    // Lendo a quantidade atual e a quantidade mínima com "nn" do banco
                    alerta.setQtdAtual(rs.getInt("quantidade"));
                    alerta.setQtdMinima(rs.getInt("quantidade_minima"));

                    lista.add(alerta);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao gerar lista de compras: " + e.getMessage());
        }

        return lista;
    }

    private String nomeProduto;
    private String nomeCategoria;
    private String lote;
    private int qtdAtual;
    private int qtdMinima;

    // Método que calcula o que precisa ser comprado para atingir o estoque mínimo
    public int getQtdComprar() {
        int diferenca = qtdMinima - qtdAtual;
        return Math.max(0, diferenca); // Evita valores negativos
    }

    // --- Getters e Setters ---
    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public String getNomeCategoria() { return nomeCategoria; }
    public void setNomeCategoria(String nomeCategoria) { this.nomeCategoria = nomeCategoria; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public int getQtdAtual() { return qtdAtual; }
    public void setQtdAtual(int qtdAtual) { this.qtdAtual = qtdAtual; }

    public int getQtdMinima() { return qtdMinima; }
    public void setQtdMinima(int qtdMinima) { this.qtdMinima = qtdMinima; }
}
