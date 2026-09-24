package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.CadastroDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;

import java.util.Objects;

public class CadastroController {
    @FXML
    private TextField txtNome;

    @FXML
    private TextField txtEmail;

    // Campos da Senha
    @FXML
    private PasswordField txtSenha;

    @FXML
    private TextField txtSenhaRevelada;

    @FXML
    private ToggleButton btnMostrarSenha;

    // Campos da Confirmação de Senha
    @FXML
    private PasswordField txtConfirmarSenha;

    @FXML
    private TextField txtConfirmarSenhaRevelada;

    @FXML
    private ToggleButton btnMostrarConfirmarSenha;

    @FXML
    private Button btnVoltar;

    @FXML
    private Button btnCadastrar;

    @FXML
    public void initialize() {
        // Sincroniza bidirecionalmente o texto digitado na Senha
        txtSenhaRevelada.textProperty().bindBidirectional(txtSenha.textProperty());

        // Sincroniza bidirecionalmente o texto digitado na Confirmação de Senha
        txtConfirmarSenhaRevelada.textProperty().bindBidirectional(txtConfirmarSenha.textProperty());
    }

    @FXML
    private void toggleMostrarSenha() {
        boolean mostrar = btnMostrarSenha.isSelected();
        txtSenhaRevelada.setVisible(mostrar);
        txtSenhaRevelada.setManaged(mostrar);
        txtSenha.setVisible(!mostrar);
        txtSenha.setManaged(!mostrar);
    }

    @FXML
    private void toggleMostrarConfirmarSenha() {
        boolean mostrar = btnMostrarConfirmarSenha.isSelected();
        txtConfirmarSenhaRevelada.setVisible(mostrar);
        txtConfirmarSenhaRevelada.setManaged(mostrar);
        txtConfirmarSenha.setVisible(!mostrar);
        txtConfirmarSenha.setManaged(!mostrar);
    }

    @FXML
    protected void voltarTela() throws Exception {
        // Ao clicar no botão, volta para a tela de login
        MainApplication.trocadorDeTelas("login.fxml");
    }

    @FXML
    protected void onCadastrar() throws Exception {
        // 1. Lê os valores digitados nos campos da tela
        String nome = txtNome.getText();
        String email = txtEmail.getText();
        String senha = txtSenha.getText();
        String confirmarSenha = txtConfirmarSenha.getText();

        // 2. Validar se tem algum campo vazio
        if (nome.isBlank() || senha.isBlank() || email.isBlank() || confirmarSenha.isBlank()) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos obrigatórios!");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha os campos obrigatórios antes de cadastrar!");
            alerta.showAndWait();
            return;
        }

        // Validar o formato do e-mail com Regex
        String emailRegex = "^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        if (!email.matches(emailRegex)) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("E-mail Inválido");
            alerta.setHeaderText(null);
            alerta.setContentText("O formato do e-mail introduzido não é válido. Por favor, insira um e-mail correto (exemplo: usuario@email.com).");
            alerta.showAndWait();
            return;
        }

        // Verificar se a senha e a confirmação são iguais
        if (!Objects.equals(senha, confirmarSenha)) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Senhas não conferem!");
            alerta.setHeaderText(null);
            alerta.setContentText("As senhas não conferem! Digite a senha novamente!");
            alerta.showAndWait();
            return;
        }

        // 3. Cria uma instância do CadastroDAO
        CadastroDAO dao = new CadastroDAO();

        // 4. Chama o método de cadastro e guarda o resultado
        boolean cadastroComSucesso = dao.cadastrarUsuario(nome, email, senha);

        // Verifica se o e-mail já existia
        if (!cadastroComSucesso) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("E-mail já cadastrado!");
            alerta.setHeaderText(null);
            alerta.setContentText("O e-mail '" + email + "' já está cadastrado no sistema. Por favor, utilize outro e-mail ou faça login.");
            alerta.showAndWait();
            return;
        }

        // 5. Informa ao usuário que o cadastro foi realizado
        Alert sucesso = new Alert(Alert.AlertType.CONFIRMATION);
        sucesso.setTitle("Cadastro realizado!");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Cadastro realizado com sucesso!");
        sucesso.showAndWait();

        // 6. Limpa os campos de texto
        txtNome.clear();
        txtEmail.clear();
        txtSenha.clear();
        txtConfirmarSenha.clear();

        MainApplication.trocadorDeTelas("login.fxml");
    }

    public Button getBtnCadastrar() {
        return btnCadastrar;
    }

    public void setBtnCadastrar(Button btnCadastrar) {
        this.btnCadastrar = btnCadastrar;
    }

    public Button getBtnVoltar() {
        return btnVoltar;
    }

    public void setBtnVoltar(Button btnVoltar) {
        this.btnVoltar = btnVoltar;
    }
}