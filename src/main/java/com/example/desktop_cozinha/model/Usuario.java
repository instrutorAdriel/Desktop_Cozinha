package com.example.desktop_cozinha.model;

import java.nio.file.attribute.UserPrincipal;

public class Usuario {
    private int ID;
    private String nome;
    private String senha;
    private String email;
    private String token;

    public void User(int ID, String nome, String senha, String email, String token ){
        this.ID = ID;
        this.nome = nome;
        this.senha = senha;
        this.email = email;
        this.token = token;


    }

    public int getID() { return ID; }
    public void setID(int ID) { this.ID = ID; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }







}
