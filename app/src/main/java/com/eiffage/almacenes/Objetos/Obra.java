package com.eiffage.almacenes.Objetos;

import android.widget.ArrayAdapter;

public class Obra {

    private String descripcion;

    public Obra(String descripcion){
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
