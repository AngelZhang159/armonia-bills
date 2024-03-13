package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Grupo implements Parcelable {

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
	private String id, titulo, descripcion;
	private ArrayList<UsuarioGrupo> usuarios;
	private double total;
	private ArrayList<Gasto> listaGastos;

	public Grupo() {

	}


	public Grupo(String id, String titulo, String descripcion, ArrayList<UsuarioGrupo> usuarios, double total, ArrayList<Gasto> listaGastos) {
		this.id = id;
		this.titulo = titulo;
		this.descripcion = descripcion;
		this.usuarios = usuarios;
		this.total = total;
		this.listaGastos = listaGastos;
	}

	protected Grupo(Parcel in) {
		id = in.readString();
		titulo = in.readString();
		descripcion = in.readString();
		usuarios = in.createTypedArrayList(UsuarioGrupo.CREATOR);
		total = in.readDouble();
		listaGastos = in.createTypedArrayList(Gasto.CREATOR);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(titulo);
		dest.writeString(descripcion);
		dest.writeTypedList(usuarios);
		dest.writeDouble(total);
		dest.writeTypedList(listaGastos);
	}
}
