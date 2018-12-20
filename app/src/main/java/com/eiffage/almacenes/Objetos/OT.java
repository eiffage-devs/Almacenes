package com.eiffage.almacenes.Objetos;

public class OT {

    private String denominacion, codOT, codObra;

    public OT(String denominacion, String codOT, String codObra){
        this.denominacion = denominacion;
        this.codOT = codOT;
        this.codObra = codObra;
    }

    public String getDenominacion() {
        return denominacion;
    }

    public String getCodOT() {
        return codOT;
    }

    public String getCodObra() {
        return codObra;
    }
}
