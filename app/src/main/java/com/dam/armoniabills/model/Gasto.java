package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Gasto implements Parcelable {

    private String titulo, descripcion, idUsuario;

    private double precio;
    ArrayList<String> listaUsuariosPagan;

    public Gasto(){

    }

    protected Gasto(Parcel in) {
        titulo = in.readString();
        descripcion = in.readString();
        idUsuario = in.readString();
        precio = in.readDouble();
        listaUsuariosPagan = in.createStringArrayList();
    }

    public static final Creator<Gasto> CREATOR = new Creator<Gasto>() {
        @Override
        public Gasto createFromParcel(Parcel in) {
            return new Gasto(in);
        }

        @Override
        public Gasto[] newArray(int size) {
            return new Gasto[size];
        }
    };

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUsuario() {
        return idUsuario;
    }

    public void setUsuario(String usuario) {
        this.idUsuario = usuario;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public ArrayList<String> getListaUsuariosPagan() {
        return listaUsuariosPagan;
    }

    public void setListaUsuariosPagan(ArrayList<String> listaUsuariosPagan) {
        this.listaUsuariosPagan = listaUsuariosPagan;
    }

    public Gasto(String titulo, String descripcion, String usuario, double precio, ArrayList<String> listaUsuariosPagan) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.idUsuario = usuario;
        this.precio = precio;
        this.listaUsuariosPagan = listaUsuariosPagan;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(descripcion);
        dest.writeString(idUsuario);
        dest.writeDouble(precio);
        dest.writeStringList(listaUsuariosPagan);
    }
}
