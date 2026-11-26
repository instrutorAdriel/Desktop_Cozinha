package com.sigec.system.sigec.Utils;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public final class TabelaUtils {

    private TabelaUtils() {
    }

    public static <T> void configurarTabelaFixa(TableView<T> tabela, double... proporcoes) {
        if (tabela == null) return;

        var colunas = tabela.getColumns();
        if (proporcoes != null && proporcoes.length == colunas.size()) {
            tabela.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
            var larguraUtil = Bindings.max(0, tabela.widthProperty().subtract(18));
            for (int i = 0; i < colunas.size(); i++) {
                TableColumn<T, ?> col = colunas.get(i);
                var largura = larguraUtil.multiply(proporcoes[i]);
                col.minWidthProperty().bind(largura);
                col.prefWidthProperty().bind(largura);
                col.maxWidthProperty().bind(largura);
            }
        }

        if (Boolean.TRUE.equals(tabela.getProperties().get("tabelaFixaConfigurada"))) {
            return;
        }
        tabela.getProperties().put("tabelaFixaConfigurada", true);

        for (TableColumn<T, ?> col : colunas) {
            String titulo = col.getText();
            if (titulo != null && !titulo.isEmpty()) {
                Label lbl = new Label(titulo);
                lbl.setMaxWidth(Double.MAX_VALUE);
                lbl.setAlignment(Pos.CENTER);
                lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-family: 'Arial Bold'; -fx-font-size: 13px; -fx-padding: 0;");
                col.setGraphic(lbl);
                col.setText("");
            }
            col.setSortable(true);
            col.setResizable(false);
            col.setReorderable(false);
        }

        // Exclui o 3º clique ("sem ordenação"): alterna apenas entre A-Z (ASC) e Z-A (DESC)
        tabela.getSortOrder().addListener((ListChangeListener<TableColumn<T, ?>>) change -> {
            if (Boolean.TRUE.equals(tabela.getProperties().get("ajustandoOrdem"))) {
                return;
            }

            if (tabela.getSortOrder().isEmpty() && !colunas.isEmpty()) {
                @SuppressWarnings("unchecked")
                TableColumn<T, ?> ultima = (TableColumn<T, ?>) tabela.getProperties().get("ultimaColunaOrdenada");
                final TableColumn<T, ?> alvo = (ultima != null) ? ultima : colunas.get(0);

                Platform.runLater(() -> {
                    tabela.getProperties().put("ajustandoOrdem", true);
                    try {
                        alvo.setSortType(TableColumn.SortType.ASCENDING);
                        tabela.getSortOrder().setAll(alvo);
                        tabela.sort();
                    } finally {
                        tabela.getProperties().put("ajustandoOrdem", false);
                    }
                });
            } else if (!tabela.getSortOrder().isEmpty()) {
                tabela.getProperties().put("ultimaColunaOrdenada", tabela.getSortOrder().get(0));
            }
        });
    }
}