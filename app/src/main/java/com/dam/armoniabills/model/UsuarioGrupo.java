package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UsuarioGrupo implements Parcelable {

	String id;
	private double deben, debes;

	public UsuarioGrupo() {

	}

	public UsuarioGrupo(double deben, double debes, String id) {
		this.deben = deben;
		this.debes = debes;
		this.id = id;
	}

	public static final Creator<UsuarioGrupo> CREATOR = new Creator<UsuarioGrupo>() {
		@Override
		public UsuarioGrupo createFromParcel(Parcel in) {
			return new UsuarioGrupo(in);
		}

		@Override
		public UsuarioGrupo[] newArray(int size) {
			return new UsuarioGrupo[size];
		}
	};

	protected UsuarioGrupo(Parcel in) {
		deben = in.readDouble();
		debes = in.readDouble();
		id = in.readString();
	}

	public double getDeben() {
		return deben;
	}

	public double getDebes() {
		return debes;
	}


	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setDeben(double deben) {
		this.deben = deben;
	}

	public void setDebes(double debes) {
		this.debes = debes;
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeDouble(deben);
		dest.writeDouble(debes);
		dest.writeString(id);
	}
}
