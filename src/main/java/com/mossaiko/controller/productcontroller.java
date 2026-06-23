package com.mossaiko.controller;

import com.mossaiko.dto.validationerror;
import com.mossaiko.dto.validationrequest;
import com.mossaiko.dto.validationresponse;
import com.mossaiko.validator.productvalidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class productcontroller {

    private final productvalidator productValidator = new productvalidator();

    @PostMapping("/validar")
    public ResponseEntity<validationresponse> validarProducto(@RequestBody validationrequest request) {
        String cadena = request != null ? request.getCadena() : "";
        List<validationerror> errors = productValidator.validate(cadena);

        String timestamp = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();

        if (errors.isEmpty()) {
            validationresponse response = new validationresponse(
                    true,
                    "Producto validado correctamente",
                    null,
                    timestamp
            );
            return ResponseEntity.ok(response);
        } else {
            validationresponse response = new validationresponse(
                    false,
                    null,
                    errors,
                    timestamp
            );
            return ResponseEntity.ok(response);
        }
    }
}