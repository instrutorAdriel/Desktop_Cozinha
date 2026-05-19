package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.MainApplication;
import com.example.desktop_cozinha.model.CadastroProdutoDAO;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;
import java.time.LocalDate;


public class CadastroProdutoController  implements Initializable {

    @FXML
    private TextField txtNomeProduto;

    @FXML
    private TextField txtQtdAtual ;

    @FXML
    private TextField txtEstoqueMinimo;
    @FXML
    private ChoiceBox<String> txtUnidadeDeMedida;

    @FXML
    private ChoiceBox <String>txtTipoProduto;


    @FXML
    private DatePicker txtDataValidade;


    @FXML
    private Button btnCadastro;

    @FXML
    private Button btnVoltar;

    public void initialize(URL url, ResourceBundle rb) {
        txtUnidadeDeMedida.getItems().addAll("KG", "LITRO", "UNIDADE");
        txtTipoProduto.getItems().addAll("PERECIVEL", "NAO_PERECIVEL", "UTENSILIO");
        txtDataValidade.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate data, boolean vazia) {
                super.updateItem(data, vazia);

                // Pega a data de hoje do sistema
                LocalDate hoje = LocalDate.now();

                // Se a data do calendário for menor (isBefore) que hoje, desabilita o clique
                if (data != null && data.isBefore(hoje)) {
                    setDisable(true);
                    //  Muda a cor de fundo das datas passadas para um cinza claro para dar feedback visual
                    setStyle("-fx-background-color: #dddddd;");
                }
            }
        });


    }



        @FXML
        protected void voltarTela() {
            // Pega a janela (Stage) onde este botão "btnVoltar" está e fecha ela
            javafx.stage.Stage stage = (javafx.stage.Stage) btnVoltar.getScene().getWindow();
            stage.close();
        }

    @FXML
    protected void onCadastrar () throws Exception {
        //1. Le os valores digitados nos campos de tela
        // getText () retorna o conteudo atual do campo como String

        String nomeProduto = txtNomeProduto.getText();
        String qtdAtual = txtQtdAtual.getText();
        String tipoProduto = txtTipoProduto.getValue();
        String unidadeDeMedida = txtUnidadeDeMedida.getValue();
        String estoqueMinimo = txtEstoqueMinimo.getText();
        // Pega o valor do calendário (pode ser a data ou pode ser null se estiver vazio)
        LocalDate dataSelecionada = txtDataValidade.getValue();

        // Verifica se o usuário escolheu uma data e se ela é anterior a hoje
        if (dataSelecionada != null && dataSelecionada.isBefore(LocalDate.now())) {
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Data de Validade Inválida");
            alerta.setHeaderText(null);
            alerta.setContentText("Você não pode definir uma data de validade que já passou!");
            alerta.showAndWait();
            return;
        }

        // Cria a variável do banco vazia por padrão
        Date dataValidade = null;

        // Só converte se o usuário realmente tiver escolhido uma data
        if (dataSelecionada != null) {
            dataValidade = Date.valueOf(dataSelecionada);
        }


        //2. Validar se tem um campo vazio
        if (nomeProduto.isBlank() || tipoProduto == null || tipoProduto.isBlank() ||
                qtdAtual.isBlank() || unidadeDeMedida == null || unidadeDeMedida.isBlank() || estoqueMinimo.isBlank()) {

            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Campos obrigatórios!");
            alerta.setHeaderText(null);
            alerta.setContentText("Por favor, preencha os campos obrigatórios antes de cadastrar o produto!");
            alerta.showAndWait();
            return;
        }

        // Substitui a vírgula pelo ponto e SALVA na própria variável ANTES de testar
        qtdAtual = qtdAtual.replace(",", ".");
        estoqueMinimo = estoqueMinimo.replace(",", ".");

        try {
            // Guarda a conversão em variáveis para podermos fazer o teste do negativo
            double valorQtdAtual = Double.parseDouble(qtdAtual);
            double valorEstoqueMinimo = Double.parseDouble(estoqueMinimo);

            //  Não aceitar valores negativos
            if (valorQtdAtual < 0 || valorEstoqueMinimo < 0) {
                Alert alerta = new Alert(Alert.AlertType.WARNING);
                alerta.setTitle("Valor Inválido");
                alerta.setHeaderText(null);
                alerta.setContentText("A quantidade atual e o estoque mínimo não podem ser negativos!");
                alerta.showAndWait();
                return; // Interrompe o metodo e não salva no banco
            }

        } catch (NumberFormatException e) {
            // Se cair aqui, é porque o usuário digitou letras ou caracteres especiais
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Formato Inválido");
            alerta.setHeaderText(null);
            alerta.setContentText("Os campos de Quantidade e Estoque Mínimo devem conter apenas números (ex: 5 ou 0.33)!");
            alerta.showAndWait();
            return; // Interrompe o metodo
        }

        // 3. Cria uma instancia do CadastroDAO
        CadastroProdutoDAO dao = new CadastroProdutoDAO();

        //4. Chama o metodo de cadastro do cliente


        dao.cadastrarProduto(nomeProduto, tipoProduto, qtdAtual, unidadeDeMedida, estoqueMinimo, dataValidade);

        //5.informa ao usuario que o cadastro foi realizado

        Alert sucesso = new Alert(Alert.AlertType.CONFIRMATION);
        sucesso.setTitle("Cadastro realizado!");
        sucesso.setHeaderText(null);
        sucesso.setContentText("Cadastro realizado com sucesso!");
        sucesso.showAndWait();

        //6. limpa os campos de texto
        txtNomeProduto.clear();
        txtQtdAtual.clear();
        txtEstoqueMinimo.clear();
        txtUnidadeDeMedida.setValue(null);
        txtTipoProduto.setValue(null);
        txtDataValidade.setValue(null);


    }

}
