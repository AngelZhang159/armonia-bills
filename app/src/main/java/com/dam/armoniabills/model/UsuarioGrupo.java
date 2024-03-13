package com.dam.armoniabills.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class UsuarioGrupo implements Parcelable {

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
	String id;
	private double deben, debes, pagado;

	public UsuarioGrupo() {

	}

	public UsuarioGrupo(double deben, double debes, double pagado, String id) {
		this.deben = deben;
		this.debes = debes;
		this.pagado = pagado;
		this.id = id;
	}

	protected UsuarioGrupo(Parcel in) {
		deben = in.readDouble();
		debes = in.readDouble();
		pagado = in.readDouble();
		id = in.readString();
	}

	public double getDeben() {
		return deben;
	}

	public double getDebes() {
		return debes;
	}

	public double getPagado() {
		return pagado;
	}

	public String getId() {
		return id;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(@NonNull Parcel dest, int flags) {
		dest.writeDouble(deben);
		dest.writeDouble(debes);
		dest.writeDouble(pagado);
		dest.writeString(id);
	}
}
