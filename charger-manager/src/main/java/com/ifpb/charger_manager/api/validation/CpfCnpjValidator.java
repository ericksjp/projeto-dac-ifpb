package com.ifpb.charger_manager.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfCnpjValidator implements ConstraintValidator<ValidCpfCnpj, String> {

    @Override
    public void initialize(ValidCpfCnpj constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        boolean isValid = isValidCpfOrCnpj(value);
        
        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("CPF ou CNPJ inválido")
                    .addConstraintViolation();
        }
        
        return isValid;
    }

    public static boolean isValidCpfOrCnpj(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

    
        String cleanValue = value.replaceAll("[^0-9]", "");

       
        if (cleanValue.length() == 11) {
            return isValidCpf(cleanValue);
        } else if (cleanValue.length() == 14) {
            return isValidCnpj(cleanValue);
        }

        return false;
    }

    private static boolean isValidCpf(String cpf) {
       
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

       
        int sum = 0;
        int multiplier = 10;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * multiplier;
            multiplier--;
        }

        int firstDigit = 11 - (sum % 11);
        if (firstDigit > 9) {
            firstDigit = 0;
        }

        if (Character.getNumericValue(cpf.charAt(9)) != firstDigit) {
            return false;
        }

       
        sum = 0;
        multiplier = 11;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * multiplier;
            multiplier--;
        }

        int secondDigit = 11 - (sum % 11);
        if (secondDigit > 9) {
            secondDigit = 0;
        }

        return Character.getNumericValue(cpf.charAt(10)) == secondDigit;
    }

    private static boolean isValidCnpj(String cnpj) {
       
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

       
        int sum = 0;
        int multiplier = 5;
        for (int i = 0; i < 8; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * multiplier;
            multiplier--;
            if (multiplier == 1) {
                multiplier = 9;
            }
        }

        int firstDigit = 11 - (sum % 11);
        if (firstDigit > 9) {
            firstDigit = 0;
        }

        if (Character.getNumericValue(cnpj.charAt(8)) != firstDigit) {
            return false;
        }

        sum = 0;
        multiplier = 6;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * multiplier;
            multiplier--;
            if (multiplier == 1) {
                multiplier = 9;
            }
        }

        int secondDigit = 11 - (sum % 11);
        if (secondDigit > 9) {
            secondDigit = 0;
        }

        return Character.getNumericValue(cnpj.charAt(9)) == secondDigit;
    }
}
