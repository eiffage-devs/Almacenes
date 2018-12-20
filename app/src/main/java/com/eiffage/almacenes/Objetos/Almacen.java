package com.eiffage.almacenes.Objetos;

public class Almacen {

    private String almacen, provincia;

    public Almacen(String provincia, String almacen){
        this.provincia = provincia;
        this.almacen = almacen;
    }

    public String getAlmacen() {
        return almacen;
    }

    public String getProvincia() {
        return provincia;
    }
}
