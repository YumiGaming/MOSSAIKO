package com.mossaiko.validator;

import com.mossaiko.dto.validationerror;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class productvalidator {
    private static class ProviderRecord{
        String rut;
        String email;
        String name;
    }

    private final List<ProviderRecord> registeredProviders;

    public productvalidator() {
        this.registeredProviders = loadProviders();
    }
    
    public List<validationerror> validate(String cadena) {
        List <validationerror> errors = new ArrayList<>();

        if (cadena == null || cadena.trim().isEmpty()) {
            errors.add(new validationerror("global", "La cadena no puede estar vacía"));
            return errors;
        }

        String[] parts = cadena.split(",", -1);
        if (parts.length < 6) {
            errors.add(new validationerror("global", "La cadena debe contener 6 campos separados por coma"));
            return errors;
        }

        String code = parts[0].trim();
        String name = parts[1].trim();
        String stockStr = parts[2].trim();
        String priceStr = parts[3].trim();
        String rut = parts[4].trim();
        String email = parts[5].trim();

        //Validacion de nombre 
        boolean nameIsValid = true;
        if(name.isEmpty()){
            errors.add(new validationerror("nombre", "El nombre no puede estar vacío"));
            nameIsValid = false;
        } else if(name.length() > 30){
            errors.add(new validationerror("nombre", "El nombre no puede tener más de 30 caracteres"));
            nameIsValid = false;
        }

        //validacion de codigo
       if (code.isEmpty()) {
            errors.add(new validationerror("codigo", "El código no puede estar vacío"));
        } else {
            // Revisar el formato del código: debe empezar con una letra mayúscula seguida de 10 dígitos
            if (!Pattern.matches("^[A-Z][0-9]{10}$", code)) {
                errors.add(new validationerror("codigo", "El código debe empezar con una letra mayúscula seguida de 10 dígitos (formato [Inicial][ddMMyyHHmm])"));
            } else {
                // Revisar que la primera letra del código coincida con la inicial del nombre del producto
                if (nameIsValid && !name.isEmpty()) {
                    char expectedInitial = Character.toUpperCase(name.charAt(0));
                    if (code.charAt(0) != expectedInitial) {
                        errors.add(new validationerror("codigo", "El código debe comenzar con la inicial del nombre del producto en mayúscula (" + expectedInitial + ")"));
                    }
                }
        // Revisar la fecha y hora 
         String datePart = code.substring(1);
                if (!isValidDateTimePart(datePart)) {
                    errors.add(new validationerror("codigo", "La fecha y hora del código no son válidas"));
                }
            }   
        }
    //Validacion del stock
     if (stockStr.isEmpty()) {
            errors.add(new validationerror("stock", "El stock no puede estar vacío"));
        } else {
            try {
                int stock = Integer.parseInt(stockStr);
                if (stock < 0) {
                    errors.add(new validationerror("stock", "El stock no debe aceptar números negativos"));
                }
            } catch (NumberFormatException e) {
                errors.add(new validationerror("stock", "El stock debe ser un valor numérico entero"));
            }
        }

        //Validacion del precio
         if (priceStr.isEmpty()) {
            errors.add(new validationerror("precio", "El precio no puede estar vacío"));
        } else {
            try {
                int price = Integer.parseInt(priceStr);
                if (price < 0) {
                    errors.add(new validationerror("precio", "El precio no debe aceptar números negativos"));
                }
            } catch (NumberFormatException e) {
                errors.add(new validationerror("precio", "El precio debe ser un valor numérico entero"));
            }
        }

        //validacion de rut del proveedor
        boolean rutIsValidFormat = false;
        if (rut.isEmpty()) {
            errors.add(new validationerror("rut_proveedor", "El RUT del proveedor no puede estar vacío"));
        } else {
            if (!Pattern.matches("^\\d{1,2}\\.\\d{3}\\.\\d{3}-[\\dkK]$", rut)) {
                errors.add(new validationerror("rut_proveedor", "El RUT del proveedor no cumple con el formato chileno estándar (XX.XXX.XXX-X)"));
            } else if (!isValidRutDv(rut)) {
                errors.add(new validationerror("rut_proveedor", "El RUT del proveedor tiene un dígito verificador inválido"));
            } else {
                rutIsValidFormat = true;
                if (!existsRutInCsv(rut)) {
                    errors.add(new validationerror("rut_proveedor", "RUT no encontrado en el listado de proveedores"));
                }
            }
        }

        //validacion de email del proveedor
        if (email.isEmpty()) {
            errors.add(new validationerror("mail_proveedor", "El mail del proveedor no puede estar vacío"));
        } else {
            if (!Pattern.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$", email)) {
                errors.add(new validationerror("mail_proveedor", "Formato de correo no válido"));
            } else {
                if (!existsEmailInCsv(email)) {
                    errors.add(new validationerror("mail_proveedor", "Mail no encontrado en el listado de proveedores"));
                }
            }
        }

        return errors;

    }
     private boolean isValidDateTimePart(String datePart) {
        try {
            int day = Integer.parseInt(datePart.substring(0, 2));
            int month = Integer.parseInt(datePart.substring(2, 4));
            int year = Integer.parseInt(datePart.substring(4, 6)) + 2000;
            int hour = Integer.parseInt(datePart.substring(6, 8));
            int minute = Integer.parseInt(datePart.substring(8, 10));

            if (month < 1 || month > 12) return false;
            if (hour < 0 || hour > 23) return false;
            if (minute < 0 || minute > 59) return false;

            int maxDays = 31;
            if (month == 4 || month == 6 || month == 9 || month == 11) {
                maxDays = 30;
            } else if (month == 2) {
                boolean isLeap = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
                maxDays = isLeap ? 29 : 28;
            }

            return day >= 1 && day <= maxDays;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isValidRutDv(String rut) {
        String cleanRut = rut.replace(".", "").replace("-", "");
        String body = cleanRut.substring(0, cleanRut.length() - 1);
        char dv = Character.toUpperCase(cleanRut.charAt(cleanRut.length() - 1));

        int sum = 0;
        int factor = 2;
        for (int i = body.length() - 1; i >= 0; i--) {
            sum += Character.getNumericValue(body.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int res = 11 - (sum % 11);
        char expectedDv;
        if (res == 11) {
            expectedDv = '0';
        } else if (res == 10) {
            expectedDv = 'K';
        } else {
            expectedDv = (char) ('0' + res);
        }
        return dv == expectedDv;
    }
    
    private boolean existsRutInCsv(String rut) {
        String normRut = normalizeRut(rut);
        return registeredProviders.stream()
                .anyMatch(p -> normalizeRut(p.rut).equals(normRut));
    }

    private boolean existsEmailInCsv(String email) {
        String cleanEmail = email.trim().toLowerCase();
        return registeredProviders.stream()
                .anyMatch(p -> p.email.trim().toLowerCase().equals(cleanEmail));
    }

     private String normalizeRut(String rut) {
        if (rut == null) return "";
        return rut.replace(".", "").replace("-", "").trim().toUpperCase();
    }

     private List<ProviderRecord> loadProviders() {
        List<ProviderRecord> list = new ArrayList<>();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("proveedores.csv")) {
            if (is == null) {
                return list;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] cols = line.split(";", -1);
                    if (cols.length >= 2) {
                        ProviderRecord pr = new ProviderRecord();
                        pr.rut = cols[0].trim();
                        pr.name = cols[1].trim();
                        pr.email = cols.length >= 3 ? cols[2].trim() : "";
                        list.add(pr);
                    }
                }
            }
        } catch (IOException e) {
            //Aqui iria un log supongo, pero no se especifica en el requerimiento
        }
        return list;
    }
}