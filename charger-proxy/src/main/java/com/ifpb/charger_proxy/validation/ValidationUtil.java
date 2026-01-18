package com.ifpb.charger_proxy.validation;

import com.ifpb.charger_proxy.exception.InvalidRequestException;

import io.github.felseje.cnpj.CnpjUtils;
import io.github.felseje.cpf.CpfUtils;
import io.github.felseje.cpf.exception.InvalidCpfException;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Utilitário para validação de dados de entrada
 */
@Component
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10,11}$");
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Valida se a string não é nula ou vazia
     */
    public void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidRequestException(fieldName, "campo obrigatório");
        }
    }

    /**
     * Valida formato de email
     */
    public void validateEmail(String email) {
        validateRequired(email, "email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new InvalidRequestException("email", "formato inválido");
        }
    }

    /**
     * Valida CPF ou CNPJ
     */
    public void validateCpfCnpj(String cpfCnpj) {
        validateRequired(cpfCnpj, "cpfCnpj");

        // remove caracteres não numéricos
        String numbers = cpfCnpj.replaceAll("[^0-9]", "");
        
        if (numbers.length() != 11 && numbers.length() != 14) {
            throw new InvalidRequestException("cpfCnpj", "deve conter 11 dígitos (CPF) ou 14 dígitos (CNPJ)");
        }
        
        // valida cpf
        if (numbers.length() == 11) {
            try {
                CpfUtils.validate(cpfCnpj);
            } catch (IllegalArgumentException | InvalidCpfException e) {
                throw new InvalidRequestException("cpfCnpj", "CPF inválido");
            }
        }
        
        // valida cnpj
        if (numbers.length() == 14) {
            try {
                CnpjUtils.validate(cpfCnpj);
            } catch (IllegalArgumentException | InvalidCpfException e) {
                throw new InvalidRequestException("cpfCnpj", "CNPJ inválido");
            }
        }
    }

    /**
     * Valida formato de telefone
     */
    public void validatePhone(String phone) {
        if (phone != null && !phone.trim().isEmpty()) {
            String numbers = phone.replaceAll("[^0-9]", "");
            if (!PHONE_PATTERN.matcher(numbers).matches()) {
                throw new InvalidRequestException("phone", "deve conter 10 ou 11 dígitos");
            }
        }
    }

    /**
     * Valida valor monetário
     */
    public void validateAmount(BigDecimal value) {
        if (value == null) {
            throw new InvalidRequestException("value", "campo obrigatório");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("value", "deve ser maior que zero");
        }
        if (value.compareTo(new BigDecimal("999999999.99")) > 0) {
            throw new InvalidRequestException("value", "valor muito alto");
        }
    }

    /**
     * Valida e parse data no formato yyyy-MM-dd
     */
    public LocalDate validateAndParseDate(String date, String fieldName) {
        validateRequired(date, fieldName);
        try {
            LocalDate parsedDate = LocalDate.parse(date, DATE_FORMATTER);
            
            // Valida que a data não está no passado
            if (fieldName.equals("dueDate") && parsedDate.isBefore(LocalDate.now())) {
                throw new InvalidRequestException(fieldName, "data de vencimento não pode estar no passado");
            }
            
            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new InvalidRequestException(fieldName, "formato de data inválido, use yyyy-MM-dd");
        }
    }

    /**
     * Valida tipo de cobrança (billingType)
     */
    public void validateBillingType(String billingType) {
        validateRequired(billingType, "billingType");
        
        if (!billingType.equals("PIX") && 
            !billingType.equals("BOLETO") && 
            !billingType.equals("CREDIT_CARD") &&
            !billingType.equals("DEBIT_CARD") &&
            !billingType.equals("UNDEFINED")) {
            throw new InvalidRequestException("billingType", 
                "deve ser PIX, BOLETO, CREDIT_CARD, DEBIT_CARD ou UNDEFINED");
        }
    }

    /**
     * Valida número de parcelas
     */
    public void validateInstallmentCount(Integer installmentCount) {
        if (installmentCount != null) {
            if (installmentCount < 1) {
                throw new InvalidRequestException("installmentCount", "deve ser maior ou igual a 1");
            }
            if (installmentCount > 12) {
                throw new InvalidRequestException("installmentCount", "deve ser menor ou igual a 12");
            }
        }
    }
}
