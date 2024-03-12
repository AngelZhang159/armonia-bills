package com.dam.armoniabills.model;

public class Gasto {

    private String titulo, descripcion, usuario;
    private double precio;

    public Gasto(String titulo, String descripcion, String usuario, double precio) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.precio = precio;
    }


}
