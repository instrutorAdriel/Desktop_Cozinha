package com.example.desktop_cozinha.Services;


public class sessaoService {
    public static String emailAtual;
    public static String nomeAtual;

       public static String getEmailAtual() {return emailAtual;}
       public static void setEmailAtual(String emailAtual) {
           sessaoService.emailAtual = emailAtual;}
       public static String getNomeAtual() {return nomeAtual;}
       public static void setNomeAtual(String nomeAtual) {
           sessaoService.nomeAtual = nomeAtual;}
}

