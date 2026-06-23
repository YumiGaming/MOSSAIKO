package com.mossaiko.validator;

import com.mossaiko.dto.validationerror;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class productvalidatortest {

    private productvalidator validator;

    @BeforeEach
    public void setUp() {
        validator = new productvalidator();
    }

    @Test
    public void testValidProduct() {
        // J1011221226: J (Jabón) + 10/11/22 12:26 (valid)
        String cadena = "J1011221226,Jabón Copito,200,990,96.954.210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertTrue(errors.isEmpty(), "Valid product should produce no validation errors");
    }

    @Test
    public void testInvalidCodeInitialMismatch() {
        // Letra inicial es A pero el producto es Jabón (J)
        String cadena = "A1011221226,Jabón Copito,200,990,96.954.210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("codigo") && e.getMensaje().toLowerCase().contains("inicial")));
    }

    @Test
    public void testInvalidCodeDateFormat() {
        // La fecha contiene caracteres no válidos (ej: mes 13)
        String cadena = "J1013221226,Jabón Copito,200,990,96.954.210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("codigo") && e.getMensaje().toLowerCase().contains("fecha")));
    }

    @Test
    public void testNameTooLong() {
        // Nombre con mas de 30 caracteres
        String longName = "Jabón Copito Super Extra Limpiador con Aroma a Rosas del Campo";
        String cadena = "J1011221226," + longName + ",200,990,96.954.210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("nombre") && e.getMensaje().contains("30")));
    }

    @ParameterizedTest  
    @ValueSource(strings = {"-5", "abc", ""})
    public void testInvalidStock(String stock) {
        String cadena = "J1011221226,Jabón Copito," + stock + ",990,96.954.210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("stock")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-10", "xyz", ""})
    public void testInvalidPrice(String price) {
        String cadena = "J1011221226,Jabón Copito,200," + price + ",96.954.210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("precio")));
    }

    @Test
    public void testInvalidRutFormat() {
        // RUT sin puntos o guion, o con formato incorrecto
        String cadena = "J1011221226,Jabón Copito,200,990,96954210-2,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("rut_proveedor") && e.getMensaje().toLowerCase().contains("formato")));
    }

    @Test
    public void testInvalidRutDigit() {
        // RUT 96.954.210-9 tiene un dígito de verificación incorrecto (deberia ser 2)
        String cadena = "J1011221226,Jabón Copito,200,990,96.954.210-9,gerencia@surlat.cl";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("rut_proveedor") && e.getMensaje().toLowerCase().contains("verificador")));
    }

    @Test
    public void testRutNotFoundInCsv() {
        // rut chileno valido pero no registrado en CSV 
        String cadena = "J1011221226,Jabón Copito,200,990,11.111.111-1,admin@example.com";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("rut_proveedor") && e.getMensaje().toLowerCase().contains("no encontrado")));
    }

    @Test
    public void testInvalidEmailFormat() {
        // Email con formato inválido
        String cadena = "J1011221226,Jabón Copito,200,990,96.954.210-2,invalid-email";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("mail_proveedor") && e.getMensaje().toLowerCase().contains("formato")));
    }

    @Test
    public void testEmailNotFoundInCsv() {
        // Email con formato válido pero no registrado en CSV
        String cadena = "J1011221226,Jabón Copito,200,990,96.954.210-2,no_registrado@example.com";
        List<validationerror> errors = validator.validate(cadena);
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> e.getCampo().equals("mail_proveedor") && e.getMensaje().toLowerCase().contains("no encontrado")));
    }
}