package com.ifpb.charger_manager.domain.repository;

import com.ifpb.charger_manager.domain.model.Customer;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de persistência de clientes
 */
@Repository
public interface CustomerRepository extends CrudRepository<Customer, UUID> {
    
    Optional<Customer> findByExternalId(String externalId);
    
    Optional<Customer> findByCpfCnpj(String cpfCnpj);
    
    Optional<Customer> findByEmail(String email);
    
    @Query("SELECT * FROM customers ORDER BY created_at DESC")
    List<Customer> findAllOrderByCreatedAtDesc();
    
    boolean existsByCpfCnpj(String cpfCnpj);
    
    boolean existsByEmail(String email);
}
