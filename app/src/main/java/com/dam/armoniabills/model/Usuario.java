package com.dam.armoniabills.model;

public class Usuario {
    private String id, nombre, email, tlf, imagenPerfil;
    double balance;

    public Usuario(String id, String nombre, String email, String tlf, String imagenPerfil, double balance) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.tlf = tlf;
        this.imagenPerfil = imagenPerfil;
        this.balance = balance;
    }
    public Usuario(String id, String nombre, String email, String tlf, String imagenPerfil) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.tlf = tlf;
        this.imagenPerfil = imagenPerfil;
    }

    public Usuario() {
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getTlf() {
        return tlf;
    }

    public void setTlf(String tlf) {
        this.tlf = tlf;
    }

    public String getImagenPerfil() {
        return imagenPerfil;
    }

    public void setImagenPerfil(String imagenPerfil) {
        this.imagenPerfil = imagenPerfil;
    }
}
