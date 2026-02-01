package com.ifpb.charger_manager.api.controller;

import com.ifpb.charger_manager.api.dto.CustomerCreateDto;
import com.ifpb.charger_manager.api.dto.CustomerResponseDto;
import com.ifpb.charger_manager.api.dto.CustomerUpdateDto;
import com.ifpb.charger_manager.domain.model.Customer;
import com.ifpb.charger_manager.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller REST para gerenciamento de clientes
 */
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private static final Logger log = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Cria um novo cliente
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerCreateDto dto) {
        log.info("POST /api/v1/customers - Creating customer: {}", dto.getName());
        
        Customer customer = customerService.createCustomer(
            dto.getName(),
            dto.getCpfCnpj(),
            dto.getEmail()
        );
        
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(customer));
    }

    /**
     * Busca um cliente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> getCustomer(@PathVariable UUID id) {
        log.info("GET /api/v1/customers/{} - Getting customer", id);

        Customer customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(toDto(customer));
    }

    /**
     * Lista todos os clientes
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        log.info("GET /api/v1/customers - Listing all customers");

        List<CustomerResponseDto> customers = customerService.getAllCustomers()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(customers);
    }

    /**
     * Atualiza um cliente
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable UUID id,
            @Valid @RequestBody CustomerUpdateDto dto) {

        log.info("PUT /api/v1/customers/{} - Updating customer", id);

        Customer customer = customerService.updateCustomer(id, dto.getName(), dto.getEmail());
        return ResponseEntity.ok(toDto(customer));
    }

    /**
     * Converte Customer para CustomerResponseDto
     */
    private CustomerResponseDto toDto(Customer customer) {
        return new CustomerResponseDto(
                customer.getId(),
                customer.getExternalId(),
                customer.getName(),
                customer.getCpfCnpj(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt());
    }
}
