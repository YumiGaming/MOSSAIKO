package com.mossaiko.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class validationresponse {
    private boolean valido;
    private String mensaje;
    private List<validationerror> errores;
    private String timestamp;

    public validationresponse() {}

    public validationresponse(boolean valido, String mensaje, List<validationerror> errores, String timestamp) {
        this.valido = valido;
        this.mensaje = mensaje;
        this.errores = errores;
        this.timestamp = timestamp;
    }

    public boolean isValido() {
        return valido;
    }

    public void setValido(boolean valido) {
        this.valido = valido;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public List<validationerror> getErrores() {
        return errores;
    }

    public void setErrores(List<validationerror> errores) {
        this.errores = errores;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}