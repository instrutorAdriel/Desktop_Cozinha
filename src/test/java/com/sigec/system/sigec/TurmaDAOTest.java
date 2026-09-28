package com.sigec.system.sigec;

import com.sigec.system.sigec.Constructors.Laboratorio;
import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.DAOS.TurmaDAO;
import com.sigec.system.sigec.DAOS.UserDAO;
import com.sigec.system.sigec.DTBConfig.ConfigDataBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TurmaDAOTest {

    @Test
    @DisplayName("Teste de normalização da situação ('Ativo' -> 'A', 'Inativo' -> 'I')")
    public void testNormalizarSituacao() {
        assertEquals("A", TurmaDAO.normalizarSituacao("Ativo"));
        assertEquals("A", TurmaDAO.normalizarSituacao("ATIVO"));
        assertEquals("A", TurmaDAO.normalizarSituacao("A"));
        assertEquals("A", TurmaDAO.normalizarSituacao(null));
        assertEquals("A", TurmaDAO.normalizarSituacao(""));

        assertEquals("I", TurmaDAO.normalizarSituacao("Inativo"));
        assertEquals("I", TurmaDAO.normalizarSituacao("INATIVO"));
        assertEquals("I", TurmaDAO.normalizarSituacao("I"));
    }

    @Test
    @DisplayName("Teste de listagem de laboratórios ativos no banco de dados")
    public void testListarLaboratoriosAtivos() {
        List<Laboratorio> labs = TurmaDAO.listarLaboratoriosAtivos();
        assertNotNull(labs, "A lista de laboratórios não deve ser nula.");
        assertFalse(labs.isEmpty(), "Deve haver pelo menos um laboratório ativo cadastrado no banco.");
        assertTrue(labs.get(0).getIdLaboratorio() > 0, "O ID do laboratório deve ser maior que 0.");
        assertNotNull(labs.get(0).getNomeLaboratorio(), "O nome do laboratório não deve ser nulo.");
        System.out.println(">>> Laboratórios ativos encontrados: " + labs.size() + " (" + labs.get(0).getNomeLaboratorio() + ")");
    }

    @Test
    @DisplayName("Teste completo de cadastro de turma e vínculo de professor com transação atômica")
    public void testCadastrarTurmaComSucesso() throws SQLException {
        try (Connection conn = ConfigDataBase.getConnection()) {
            assertNotNull(conn, "A conexão com o banco não deve ser nula.");

            // 1. Obter laboratório ativo
            List<Laboratorio> labs = TurmaDAO.listarLaboratoriosAtivos();
            assertFalse(labs.isEmpty(), "Deve existir laboratório ativo.");
            Laboratorio lab = labs.get(0);
            int idLaboratorio = lab.getIdLaboratorio();

            // 2. Obter professor disponível
            List<User> instrutores = UserDAO.listarInstrutores();
            assertNotNull(instrutores, "Lista de instrutores não deve ser nula.");
            assertFalse(instrutores.isEmpty(), "Deve existir pelo menos um instrutor/usuário.");
            int idProfessor = instrutores.get(0).getIdUsuario();

            // 3. Cadastrar turma
            String nomeTurma = "Gastronomia Teste Unitário " + System.currentTimeMillis();
            String situacao = "Ativo";

            int idTurmaCriada = TurmaDAO.cadastrarTurma(nomeTurma, situacao, idLaboratorio, idProfessor);
            assertTrue(idTurmaCriada > 0, "O ID da turma gerado deve ser maior que zero.");

            // 4. Validar dados gravados na tabela 'turma'
            String sqlTurma = "SELECT nome_turma, situacao, id_laboratorio FROM turma WHERE id_turma = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlTurma)) {
                stmt.setInt(1, idTurmaCriada);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "A turma deve ser encontrada no banco.");
                    assertEquals(nomeTurma, rs.getString("nome_turma"));
                    assertEquals("A", rs.getString("situacao"));
                    assertEquals(idLaboratorio, rs.getInt("id_laboratorio"));
                }
            }

            // 5. Validar vínculo gravado na tabela 'usuario_turma'
            String sqlVinculo = "SELECT id_usuario, id_turma FROM usuario_turma WHERE id_turma = ? AND id_usuario = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sqlVinculo)) {
                stmt.setInt(1, idTurmaCriada);
                stmt.setInt(2, idProfessor);
                try (ResultSet rs = stmt.executeQuery()) {
                    assertTrue(rs.next(), "O vínculo entre professor e turma deve existir na tabela usuario_turma.");
                }
            }

            System.out.printf(">>> SUCESSO: Turma #%d vinculada ao professor #%d com situação 'A'%n", idTurmaCriada, idProfessor);

            // 6. Limpeza do registro de teste
            try (PreparedStatement delVinculo = conn.prepareStatement("DELETE FROM usuario_turma WHERE id_turma = ?")) {
                delVinculo.setInt(1, idTurmaCriada);
                delVinculo.executeUpdate();
            }
            try (PreparedStatement delTurma = conn.prepareStatement("DELETE FROM turma WHERE id_turma = ?")) {
                delTurma.setInt(1, idTurmaCriada);
                delTurma.executeUpdate();
            }
        }
    }
}
