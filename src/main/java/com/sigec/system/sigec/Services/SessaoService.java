package com.sigec.system.sigec.Services;

import com.sigec.system.sigec.Constructors.User;

/**
 * Gerenciador de sessão do usuário logado na aplicação SIGEC.
 */
public final class SessaoService {

    private static User usuarioAtual = null;

    private SessaoService() {
        // Construtor privado para utilitário estático de sessão
    }

    /**
     * Define o usuário atualmente autenticado no sistema.
     *
     * @param user Usuário autenticado
     */
    public static void iniciarSessao(User user) {
        usuarioAtual = user;
    }

    /**
     * Alias para iniciarSessao, definindo o usuário logado.
     *
     * @param user Usuário autenticado
     */
    public static void setUsuarioLogado(User user) {
        iniciarSessao(user);
    }

    /**
     * Encerra a sessão atual do usuário (logout).
     */
    public static void encerrarSessao() {
        usuarioAtual = null;
    }

    /**
     * Retorna o usuário da sessão ativa.
     *
     * @return {@link User} atual ou null se não houver sessão ativa
     */
    public static User getUsuarioAtual() {
        return usuarioAtual;
    }

    /**
     * Alias para getUsuarioAtual.
     *
     * @return {@link User} atual ou null se não houver sessão ativa
     */
    public static User getUsuarioLogado() {
        return getUsuarioAtual();
    }

    /**
     * Verifica se existe um usuário logado.
     *
     * @return true se logado, false caso contrário
     */
    public static boolean isLogado() {
        return usuarioAtual != null;
    }


    public static int getIdUsuarioLogado() {
        return usuarioAtual != null ? usuarioAtual.getIdUsuario() : -1;
    }

    public static String getNomeExibicao() {
        if (usuarioAtual != null && usuarioAtual.getNome() != null && !usuarioAtual.getNome().isBlank()) {
            return usuarioAtual.getNome();
        }
        return "Administrador";
    }


    public static String getNomeUsuarioLogado() {
        return getNomeExibicao();
    }
}