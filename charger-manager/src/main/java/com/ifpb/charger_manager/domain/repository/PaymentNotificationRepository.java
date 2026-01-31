package com.ifpb.charger_manager.domain.repository;

import com.ifpb.charger_manager.domain.model.PaymentNotification;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para operações de persistência de notificações de pagamento
 */
@Repository
public interface PaymentNotificationRepository extends CrudRepository<PaymentNotification, UUID> {
    
    @Query("SELECT * FROM payment_notifications WHERE charge_id = :chargeId ORDER BY received_at DESC")
    List<PaymentNotification> findByChargeId(@Param("chargeId") UUID chargeId);
    
    @Query("SELECT * FROM payment_notifications WHERE processed = false ORDER BY received_at")
    List<PaymentNotification> findUnprocessed();
    
    Optional<PaymentNotification> findByExternalEventId(String externalEventId);
}
