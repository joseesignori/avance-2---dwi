package pe.clinicasanfelipe.citas.exception;

import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException {

    private final HttpStatus estado;

    public ApiException(String mensaje, HttpStatus estado) {
        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}
