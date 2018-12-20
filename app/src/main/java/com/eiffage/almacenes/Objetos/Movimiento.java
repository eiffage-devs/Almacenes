package com.eiffage.almacenes.Objetos;

public class Movimiento {

    private String tipoMov, fecha, origen, destino, obra, ticket;

    public Movimiento(String tipoMov, String fecha, String origen, String destino, String obra, String ticket){
        this.tipoMov = tipoMov;
        this.fecha = fecha;
        this.origen = origen;
        this.destino = destino;
        this.obra = obra;
        this.ticket = ticket;
    }

    public String getTipoMov() {
        return tipoMov;
    }

    public String getFecha() {
        return fecha;
    }

    public String getOrigen() {
        return origen;
    }

    public String getDestino() {
        return destino;
    }

    public String getObra() {
        return obra;
    }

    public String getTicket() {
        return ticket;
    }

    @Override
    public String toString() {
        return tipoMov;
    }
}
