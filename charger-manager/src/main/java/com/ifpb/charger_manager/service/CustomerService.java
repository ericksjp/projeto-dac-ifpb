package com.ifpb.charger_manager.service;

import com.ifpb.charger_manager.domain.model.Customer;
import com.ifpb.charger_manager.domain.repository.CustomerRepository;
import com.ifpb.charger_manager.exception.CustomerAlreadyExistsException;
import com.ifpb.charger_manager.exception.CustomerNotFoundException;
import com.ifpb.charger_manager.exception.InvalidRequestException;
import com.ifpb.charger_manager.infra.soap.ChargeProxyClient;
import com.ifpb.charger_manager.ws.customer.v1.RegisterCustomerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Serviço para gerenciamento de clientes
 */
@Service
public class CustomerService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);
    
    private final CustomerRepository customerRepository;
    private final ChargeProxyClient chargeProxyClient;

    public CustomerService(CustomerRepository customerRepository, ChargeProxyClient chargeProxyClient) {
        this.customerRepository = customerRepository;
        this.chargeProxyClient = chargeProxyClient;
    }
    
    /**
     * Cria um novo cliente localmente e registra no Asaas via charge-proxy
     */
    @Transactional
    public Customer createCustomer(String name, String cpfCnpj, String email) {
        log.info("Creating customer: name={}, cpfCnpj={}, email={}", name, cpfCnpj, email);
        
        // Validações
        validateCustomerData(name, cpfCnpj, email);
        
        // Verifica se já existe
        if (customerRepository.existsByCpfCnpj(cpfCnpj)) {
            throw new CustomerAlreadyExistsException("Cliente com CPF/CNPJ " + cpfCnpj + " já existe");
        }
        
        if (customerRepository.existsByEmail(email)) {
            throw new CustomerAlreadyExistsException("Cliente com email " + email + " já existe");
        }
        
        // Cria o cliente localmente primeiro
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName(name);
        customer.setCpfCnpj(cpfCnpj);
        customer.setEmail(email);
        customer.setCreatedAt(LocalDateTime.now());
        customer.setUpdatedAt(LocalDateTime.now());
        
        // Salva localmente
        customer = customerRepository.save(customer);
        
        try {
            // Registra no Asaas via charge-proxy
            RegisterCustomerResponse response = chargeProxyClient.registerCustomer(
                customer.getId().toString(),
                name,
                cpfCnpj,
                email
            );
            
            // Atualiza com o ID externo do Asaas
            customer.setExternalId(response.getCustomerExternalId());
            customer = customerRepository.save(customer);
            
            log.info("Customer created successfully: id={}, externalId={}", 
                     customer.getId(), customer.getExternalId());
            
        } catch (Exception e) {
            log.error("Error registering customer in Asaas: {}", e.getMessage(), e);
            // Poderia fazer rollback aqui, mas vamos manter o cliente local
            // mesmo se falhar no Asaas, para retry posterior
        }
        
        return customer;
    }
    
    /**
     * Busca um cliente por ID
     */
    public Customer getCustomerById(UUID id) {
        return customerRepository.findById(id)
            .orElseThrow(() -> new CustomerNotFoundException("Cliente não encontrado: " + id));
    }
    
    /**
     * Busca um cliente por CPF/CNPJ
     */
    public Customer getCustomerByCpfCnpj(String cpfCnpj) {
        return customerRepository.findByCpfCnpj(cpfCnpj)
            .orElseThrow(() -> new CustomerNotFoundException("Cliente não encontrado com CPF/CNPJ: " + cpfCnpj));
    }
    
    /**
     * Lista todos os clientes
     */
    public List<Customer> getAllCustomers() {
        return customerRepository.findAllOrderByCreatedAtDesc();
    }
    
    /**
     * Atualiza dados de um cliente
     */
    @Transactional
    public Customer updateCustomer(UUID id, String name, String email) {
        log.info("Updating customer: id={}", id);
        
        Customer customer = getCustomerById(id);
        
        if (name != null && !name.isBlank()) {
            customer.setName(name);
        }
        
        if (email != null && !email.isBlank()) {
            // Verifica se o email já está em uso por outro cliente
            customerRepository.findByEmail(email).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new CustomerAlreadyExistsException("Email já está em uso: " + email);
                }
            });
            customer.setEmail(email);
        }
        
        customer.setUpdatedAt(LocalDateTime.now());
        customer = customerRepository.save(customer);
        
        log.info("Customer updated successfully: id={}", id);
        return customer;
    }
    
    /**
     * Valida os dados do cliente
     */
    private void validateCustomerData(String name, String cpfCnpj, String email) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("Nome é obrigatório");
        }
        
        if (cpfCnpj == null || cpfCnpj.isBlank()) {
            throw new InvalidRequestException("CPF/CNPJ é obrigatório");
        }
        
        if (email == null || email.isBlank()) {
            throw new InvalidRequestException("Email é obrigatório");
        }
        
        // Validação básica de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidRequestException("Email inválido");
        }
    }
}
