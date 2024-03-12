package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Gasto implements Parcelable {

    private String titulo, descripcion, usuario;
    private double precio;

    public Gasto(){

    }

    public Gasto(String titulo, String descripcion, String usuario, double precio) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.usuario = usuario;
        this.precio = precio;
    }


    protected Gasto(Parcel in) {
        titulo = in.readString();
        descripcion = in.readString();
        usuario = in.readString();
        precio = in.readDouble();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(titulo);
        dest.writeString(descripcion);
        dest.writeString(usuario);
        dest.writeDouble(precio);
    }
}
