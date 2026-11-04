package com.sigec.system.sigec.Constructors;

import java.util.Objects;

public class ListaEstoque {

    private int idInsumo;
    private int idProduto;
    private String nomeProduto;
    private String lote;
    private String tipoProduto;
    private double quantidadeProduto;
    private String unidadeProduto;

    public ListaEstoque() {
    }

    public ListaEstoque(int idInsumo, int idProduto, String nomeProduto, String lote, String tipoProduto, double quantidadeProduto, String unidadeProduto) {
        this.idInsumo = idInsumo;
        this.idProduto = idProduto;
        this.nomeProduto = nomeProduto;
        this.lote = lote;
        this.tipoProduto = tipoProduto;
        this.quantidadeProduto = quantidadeProduto;
        this.unidadeProduto = unidadeProduto;
    }

    public int getIdInsumo() {
        return idInsumo;
    }

    public void setIdInsumo(int idInsumo) {
        this.idInsumo = idInsumo;
    }

    public int getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(int idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getTipoProduto() {
        return tipoProduto;
    }

    public void setTipoProduto(String tipoProduto) {
        this.tipoProduto = tipoProduto;
    }

    public double getQuantidadeProduto() {
        return quantidadeProduto;
    }

    public void setQuantidadeProduto(double quantidadeProduto) {
        this.quantidadeProduto = quantidadeProduto;
    }

    public String getUnidadeProduto() {
        return unidadeProduto;
    }

    public void setUnidadeProduto(String unidadeProduto) {
        this.unidadeProduto = unidadeProduto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListaEstoque that = (ListaEstoque) o;

        return idInsumo == that.idInsumo;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInsumo);
    }

    @Override
    public String toString() {
        return "ListaEstoque{" +
                "idInsumo=" + idInsumo +
                ", idProduto=" + idProduto +
                ", nomeProduto='" + nomeProduto + '\'' +
                ", lote='" + lote + '\'' +
                ", tipoProduto='" + tipoProduto + '\'' +
                ", quantidadeProduto=" + quantidadeProduto +
                ", unidadeProduto='" + unidadeProduto + '\'' +
                '}';
    }
}