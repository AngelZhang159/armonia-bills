package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Gasto implements Parcelable {


	private String titulo, descripcion, idUsuario;
	private double precio;
	ArrayList<String> listaUsuariosPagan;
	private String id;

	public Gasto() {

	}

	public Gasto(String titulo, String descripcion, String idUsuario, double precio, ArrayList<String> listaUsuariosPagan, String id) {
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.idUsuario = idUsuario;
		this.precio = precio;
		this.listaUsuariosPagan = listaUsuariosPagan;
		this.id = id;
	}

	protected Gasto(Parcel in) {
		titulo = in.readString();
		descripcion = in.readString();
		idUsuario = in.readString();
		precio = in.readDouble();
		listaUsuariosPagan = in.createStringArrayList();
		id = in.readString();
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

	public String getIdUsuario() {
		return idUsuario;
	}

	public double getPrecio() {
		return precio;
	}

	public ArrayList<String> getListaUsuariosPagan() {
		return listaUsuariosPagan;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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
		dest.writeString(id);
	}
}
