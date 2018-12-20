package com.eiffage.almacenes.Objetos;

public class Linea {

    private String codigo, lote, descripcion;
    private int unidades;

    public Linea(String codigo, int unidades, String lote, String descripcion){
        this.codigo = codigo;
        this.unidades = unidades;
        this.descripcion = descripcion;

        if(lote.equals("")){
            this.lote = "-";
        }
        else {
            this.lote = lote;
        }
    }

    @Override
    public String toString() {
        return "" + codigo + ", " + unidades + ", " + descripcion + ", " + lote;
    }

    public String getCodigo() {
        return codigo;
    }

    public int getUnidades() {
        return unidades;
    }

    public String getLote() {
        return lote;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
