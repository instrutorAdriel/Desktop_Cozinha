package com.sigec.system.sigec.Controllers;

import com.sigec.system.sigec.Constructors.User;
import com.sigec.system.sigec.Constructors.Utensilio;
import com.sigec.system.sigec.DAOS.CategoriaUtensilioDAO;
import com.sigec.system.sigec.DAOS.LaboratorioDAO;
import com.sigec.system.sigec.DAOS.UnidadeDAO;
import com.sigec.system.sigec.DAOS.UtensilioDAO;
import com.sigec.system.sigec.Services.SessaoService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class CadastroUtensilioController implements Initializable {

    @FXML private TextField txtNomeUtensilio;
    @FXML private TextField txtQuantidade;
    @FXML private TextField txtPatrimonio;
    @FXML private ChoiceBox<String> cbUnidade;
    @FXML private ChoiceBox<String> cbCategoria;
    @FXML private ChoiceBox<String> cbLaboratorio;

    @FXML private Button btnVoltar;
    @FXML private Button btnSalvar;

    private Map<String, Integer> mapaUnidades = new LinkedHashMap<>();
    private Map<String, Integer> mapaCategorias;
    private Map<String, Integer> mapaLaboratorios;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (cbUnidade != null) {
            User usuarioLogado = SessaoService.getUsuarioAtual();
            if (usuarioLogado != null && usuarioLogado.getIdUsuario() > 0) {
                mapaUnidades = UnidadeDAO.listarUnidadesPorUsuarioMap(usuarioLogado.getIdUsuario());
            }
            cbUnidade.setItems(FXCollections.observableArrayList(mapaUnidades.keySet()));
            if (!mapaUnidades.isEmpty()) {
                cbUnidade.setValue(mapaUnidades.keySet().iterator().next());
            }
        }

        if (cbLaboratorio != null) {
            mapaLaboratorios = LaboratorioDAO.listarLaboratoriosAtivosMap();
            cbLaboratorio.setItems(FXCollections.observableArrayList(mapaLaboratorios.keySet()));

            if (!mapaLaboratorios.isEmpty()) {
                cbLaboratorio.setValue(mapaLaboratorios.keySet().iterator().next());
            }
        }

        if (cbCategoria != null) {
            mapaCategorias = CategoriaUtensilioDAO.listarCategoriasAtivas();
            cbCategoria.setItems(FXCollections.observableArrayList(mapaCategorias.keySet()));

            if (!mapaCategorias.isEmpty()) {
                cbCategoria.setValue(mapaCategorias.keySet().iterator().next());
            }
        }
    }

    @FXML
    public void onSalvar(ActionEvent event) {
        String nome = txtNomeUtensilio.getText();
        String strQtd = txtQuantidade.getText();
        String strPatrimonio = txtPatrimonio.getText();

        String nomeUnidade = cbUnidade != null ? cbUnidade.getValue() : null;
        String nomeCategoria = cbCategoria.getValue();
        String nomeLaboratorio = cbLaboratorio.getValue();

        if (nome == null || nome.trim().isEmpty() || strQtd == null || strQtd.trim().isEmpty() ||
                strPatrimonio == null || strPatrimonio.trim().isEmpty() ||
                nomeUnidade == null || nomeCategoria == null || nomeLaboratorio == null) {

            exibirAlerta(Alert.AlertType.WARNING, "Atenção", "Preencha todos os campos obrigatórios.");
            return;
        }

        try {
            int qtd = Integer.parseInt(strQtd.trim());
            int patrimonio = Integer.parseInt(strPatrimonio.trim());

            int idUnidade = mapaUnidades.getOrDefault(nomeUnidade, 0);
            int idCategoria = mapaCategorias.get(nomeCategoria);
            int idLaboratorio = mapaLaboratorios.get(nomeLaboratorio);

            Utensilio novoUtensilio = new Utensilio(
                    0,
                    nome.trim(),
                    nomeLaboratorio,
                    "A",
                    qtd,
                    qtd,
                    patrimonio,
                    idUnidade,
                    idCategoria,
                    idLaboratorio
            );

            boolean sucesso = UtensilioDAO.adicionarUtensilio(novoUtensilio);

            if (sucesso) {
                exibirAlerta(Alert.AlertType.INFORMATION, "Sucesso", "Utensílio adicionado com sucesso!");
                fecharModal();
            } else {
                exibirAlerta(Alert.AlertType.ERROR, "Erro", "Não foi possível salvar as informações na base de dados.");
            }
        } catch (NumberFormatException e) {
            exibirAlerta(Alert.AlertType.ERROR, "Erro", "Quantidade e Património devem ser números válidos.");
        }
    }

    @FXML
    public void voltarTela(ActionEvent event) {
        fecharModal();
    }

    private void fecharModal() {
        Stage stage = (Stage) btnVoltar.getScene().getWindow();
        stage.close();
    }

    private void exibirAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}
