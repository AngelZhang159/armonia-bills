package com.dam.armoniabills.model;

import java.util.ArrayList;

public class Grupo {

    private String titulo, descripcion;
    private ArrayList<UsuarioGrupo> usuarios;
    private double total;
    private ArrayList<Gasto> listaGastos;

    public Grupo(String titulo, String descripcion, ArrayList<UsuarioGrupo> usuarios, double total, ArrayList<Gasto> listaGastos) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuarios = usuarios;
        this.total = total;
        this.listaGastos = listaGastos;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ArrayList<UsuarioGrupo> getUsuarios() {
        return usuarios;
    }

    public double getTotal() {
        return total;
    }

    public ArrayList<Gasto> getListaGastos() {
        return listaGastos;
    }
}
