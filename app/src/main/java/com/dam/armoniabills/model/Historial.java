package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Historial implements Parcelable {
	private String id, nombreGrupo, descripcion, imagenPerfil;
	long tiempo;

	public Historial(String id, String nombreGrupo, String descripcion, String imagenPerfil, long tiempo) {
		this.id = id;
		this.nombreGrupo = nombreGrupo;
		this.descripcion = descripcion;
		this.imagenPerfil = imagenPerfil;
		this.tiempo = tiempo;
	}

	public Historial() {
	}

	protected Historial(Parcel in) {
		id = in.readString();
		nombreGrupo = in.readString();
		descripcion = in.readString();
		imagenPerfil = in.readString();
		tiempo = in.readLong();
	}

	public static final Creator<Historial> CREATOR = new Creator<Historial>() {
		@Override
		public Historial createFromParcel(Parcel in) {
			return new Historial(in);
		}

		@Override
		public Historial[] newArray(int size) {
			return new Historial[size];
		}
	};

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getNombreGrupo() {
		return nombreGrupo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public String getImagenPerfil() {
		return imagenPerfil;
	}

	public long getTiempo() {
		return tiempo;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(nombreGrupo);
		dest.writeString(descripcion);
		dest.writeString(imagenPerfil);
		dest.writeLong(tiempo);
	}
}
