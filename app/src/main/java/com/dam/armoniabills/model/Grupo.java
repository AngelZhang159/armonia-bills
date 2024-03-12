package com.dam.armoniabills.model;

import java.util.List;

public class Grupo {

    private String titulo, descripcion;
    private List<UsuarioGrupo> usuarios;
    private double total;
    private List<Gasto> listaGastos;

    public Grupo(String titulo, String descripcion, List<UsuarioGrupo> usuarios, double total, List<Gasto> listaGastos) {
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

    public List<UsuarioGrupo> getUsuarios() {
        return usuarios;
    }

    public double getTotal() {
        return total;
    }

    public List<Gasto> getListaGastos() {
        return listaGastos;
    }
}
