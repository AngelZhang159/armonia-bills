package com.dam.armoniabills.model;

public class HistorialBalance {

    String id, nombre, accion;
    double cantidad;

    public HistorialBalance(String id, String nombre, String accion, double cantidad) {
        this.id = id;
        this.nombre = nombre;
        this.accion = accion;
        this.cantidad = cantidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }

    public double getCantidad() {
        return cantidad;
    }

    public void setCantidad(double cantidad) {
        this.cantidad = cantidad;
    }
}
