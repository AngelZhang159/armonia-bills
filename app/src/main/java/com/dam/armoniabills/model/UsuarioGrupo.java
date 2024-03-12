package com.dam.armoniabills.model;

public class UsuarioGrupo {

    private double deben, debes, pagado;
    String email;

    public UsuarioGrupo(double deben, double debes, double pagado, String email) {
        this.deben = deben;
        this.debes = debes;
        this.pagado = pagado;
        this.email = email;
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

    public String getEmail() {
        return email;
    }
}
