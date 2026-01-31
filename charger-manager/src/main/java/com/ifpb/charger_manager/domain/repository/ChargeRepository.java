package com.ifpb.charger_manager.domain.repository;

import com.ifpb.charger_manager.domain.enums.ChargeStatus;
import com.ifpb.charger_manager.domain.model.Charge;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de persistência de cobranças
 */
@Repository
public interface ChargeRepository extends CrudRepository<Charge, UUID> {
    
    Optional<Charge> findByExternalId(String externalId);
    
    @Query("SELECT * FROM charges WHERE customer_id = :customerId ORDER BY created_at DESC")
    List<Charge> findByCustomerId(@Param("customerId") UUID customerId);
    
    @Query("SELECT * FROM charges WHERE installment_id = :installmentId ORDER BY installment_number")
    List<Charge> findByInstallmentId(@Param("installmentId") String installmentId);
    
    @Query("SELECT * FROM charges WHERE status = :status ORDER BY due_date")
    List<Charge> findByStatus(@Param("status") String status);
    
    @Query("SELECT * FROM charges ORDER BY created_at DESC")
    List<Charge> findAllOrderByCreatedAtDesc();
}
