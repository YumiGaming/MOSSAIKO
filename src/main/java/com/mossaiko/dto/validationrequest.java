package com.mossaiko.dto;

public class validationrequest {
    private String cadena;

    public validationrequest() {}

    public validationrequest(String cadena) {
        this.cadena = cadena;
    }

    public String getCadena() {
        return cadena;
    }

    public void setCadena(String cadena) {
        this.cadena = cadena;
    }
}