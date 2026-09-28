package com.sigec.system.sigec.DAOS;

import com.sigec.system.sigec.Constructors.Laboratorio;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Objeto de Acesso a Dados (DAO) para operações de Turmas, Laboratórios e vínculos no banco de dados.
 */
public final class TurmaDAO {

    private TurmaDAO() {
        // Construtor privado para utilitário estático de DAO
    }

    /**
     * Normaliza a situação selecionada pelo admin ("Ativo", "Inativo", etc.) para o padrão 'A' / 'I' do banco CHAR(1).
     *
     * @param situacao String vinda do formulário
     * @return "A" para ativo ou "I" para inativo
     */
    public static String normalizarSituacao(String situacao) {
        if (situacao == null || situacao.isBlank()) {
            return "A";
        }
        String s = situacao.trim().toUpperCase();
        if (s.startsWith("I") || s.equals("0") || s.equals("INATIVO")) {
            return "I";
        }
        return "A";
    }

    /**
     * Cadastra uma nova turma e vincula o professor responsável na tabela associativa 'usuario_turma'.
     * Executa tudo em uma transação atômica (commit/rollback) para garantir consistência relacional.
     *
     * @param nomeTurma     Nome digitado pelo admin no formulário (ex: "Cozinha Básica - 2026.1")
     * @param situacao      Opção selecionada pelo admin ("Ativo", "Inativo")
     * @param idLaboratorio ID do laboratório escolhido pelo admin (chave estrangeira de 'laboratorio')
     * @param idProfessor   ID do instrutor/usuário selecionado na lista (chave estrangeira de 'usuario')
     * @return O id_turma gerado automaticamente pelo banco de dados (AUTO_INCREMENT)
     * @throws SQLException Caso ocorra erro de conexão, integridade ou execução SQL
     */
    public static int cadastrarTurma(String nomeTurma, String situacao, int idLaboratorio, int idProfessor) throws SQLException {
        if (nomeTurma == null || nomeTurma.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome da turma é obrigatório.");
        }
        if (idLaboratorio <= 0) {
            throw new IllegalArgumentException("Laboratório inválido selecionado.");
        }
        if (idProfessor <= 0) {
            throw new IllegalArgumentException("Professor responsável inválido selecionado.");
        }

        String situacaoNormalizada = normalizarSituacao(situacao);

        String sqlTurma = "INSERT INTO turma (nome_turma, situacao, id_laboratorio) VALUES (?, ?, ?)";
        String sqlUsuarioTurma = "INSERT INTO usuario_turma (id_usuario, id_turma) VALUES (?, ?)";

        try (Connection conn = ConfigDataBase.getConnection()) {
            // Desativa auto-commit para garantir atomicidade transacional (ACID)
            conn.setAutoCommit(false);
            int idTurmaGerada = -1;

            try {
                // 1. Inserir a Turma e obter a chave primária gerada (id_turma)
                try (PreparedStatement stmtTurma = conn.prepareStatement(sqlTurma, Statement.RETURN_GENERATED_KEYS)) {
                    stmtTurma.setString(1, nomeTurma.trim());
                    stmtTurma.setString(2, situacaoNormalizada);
                    stmtTurma.setInt(3, idLaboratorio);

                    stmtTurma.executeUpdate();

                    try (ResultSet rs = stmtTurma.getGeneratedKeys()) {
                        if (rs.next()) {
                            idTurmaGerada = rs.getInt(1);
                        } else {
                            throw new SQLException("Falha ao recuperar o ID gerado da turma.");
                        }
                    }
                }

                // 2. Vincular o Professor responsável à Turma na tabela 'usuario_turma'
                try (PreparedStatement stmtVinculo = conn.prepareStatement(sqlUsuarioTurma)) {
                    stmtVinculo.setInt(1, idProfessor);
                    stmtVinculo.setInt(2, idTurmaGerada);
                    stmtVinculo.executeUpdate();
                }

                // Efetiva a transação no banco de dados
                conn.commit();
                return idTurmaGerada;

            } catch (SQLException ex) {
                // Reverte tudo em caso de falha em qualquer etapa
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    ex.addSuppressed(rollbackEx);
                }
                throw ex;
            } finally {
                // Restaura o modo padrão da conexão
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException ignored) {
                }
            }
        }
    }

    /**
     * Retorna a lista de laboratórios ativos cadastrados no banco de dados.
     * Permite que a tela (ChoiceBox) liste os laboratórios reais disponíveis para seleção.
     *
     * @return Lista de laboratórios ativos
     */
    public static List<Laboratorio> listarLaboratoriosAtivos() {
        List<Laboratorio> laboratorios = new ArrayList<>();
        String sql = "SELECT id_laboratorio, nome_laboratorio, capacidade, situacao, id_unidade " +
                     "FROM laboratorio WHERE UPPER(situacao) = 'A' OR UPPER(situacao) = 'ATIVO' " +
                     "ORDER BY nome_laboratorio ASC";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Laboratorio lab = new Laboratorio(
                        rs.getInt("id_laboratorio"),
                        rs.getString("nome_laboratorio"),
                        rs.getInt("capacidade"),
                        rs.getString("situacao"),
                        rs.getInt("id_unidade")
                );
                laboratorios.add(lab);
            }
        } catch (SQLException e) {
            System.err.println("Aviso: Falha ao listar laboratórios do banco de dados: " + e.getMessage());
        }

        return laboratorios;
    }

    /**
     * Busca o ID do laboratório pelo seu nome.
     * Útil quando o ChoiceBox da interface disponibiliza os nomes como String.
     *
     * @param nomeLaboratorio Nome do laboratório selecionado na interface
     * @return ID do laboratório correspondente ou -1 se não encontrado
     * @throws SQLException Em caso de erro na consulta
     */
    public static int buscarIdLaboratorioPorNome(String nomeLaboratorio) throws SQLException {
        if (nomeLaboratorio == null || nomeLaboratorio.trim().isEmpty()) {
            return -1;
        }

        String sql = "SELECT id_laboratorio FROM laboratorio WHERE LOWER(TRIM(nome_laboratorio)) = LOWER(TRIM(?)) LIMIT 1";

        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nomeLaboratorio.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_laboratorio");
                }
            }
        }

        // Se não encontrou pelo nome exato, tenta o primeiro laboratório ativo como fallback
        String sqlFallback = "SELECT id_laboratorio FROM laboratorio WHERE UPPER(situacao) = 'A' LIMIT 1";
        try (Connection conn = ConfigDataBase.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlFallback);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("id_laboratorio");
            }
        }

        return -1;
    }
}
