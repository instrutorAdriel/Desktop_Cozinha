package com.example.desktop_cozinha.controller;

import com.example.desktop_cozinha.model.ListaEstoqueDAO;

import java.util.List;

public class ListaController {


        private ListaEstoqueDAO listaEstoqueDAO;

        // Injeção via construtor (boa prática)
        public ListaController() {
            this.listaEstoqueDAO = new listaEstoqueDAO();
        }

        public void setListaEstoqueDAO() {
            IO.println("Lista de Usuários:");

            List<String> resultados = listaEstoqueDAO.lerTodos();

            for (String user : resultados) {
                IO.println(user);
            }

            IO.println("--- FIM DA LISTA ---");
        }
    }


