package com.dam.armoniabills.model;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.List;

public class Grupo implements Parcelable {

    private String titulo, descripcion;
    private ArrayList<UsuarioGrupo> usuarios;
    private double total;
    private ArrayList<Gasto> listaGastos;

    public Grupo(){

    }

    public Grupo(String titulo, String descripcion, ArrayList<UsuarioGrupo> usuarios, double total, ArrayList<Gasto> listaGastos) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuarios = usuarios;
        this.total = total;
        this.listaGastos = listaGastos;
    }

    protected Grupo(Parcel in) {
        titulo = in.readString();
        descripcion = in.readString();
        total = in.readDouble();
    }

    public static final Creator<Grupo> CREATOR = new Creator<Grupo>() {
        @Override
        public Grupo createFromParcel(Parcel in) {
            return new Grupo(in);
        }

        @Override
        public Grupo[] newArray(int size) {
            return new Grupo[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(descripcion);
        dest.writeDouble(total);
    }
}
