package com.ifpb.charger_proxy.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ifpb.charger_proxy.domain.PaymentEvent;

public interface PaymentEventRepository extends CrudRepository<PaymentEvent, UUID> {
    @Query("""
            SELECT *
            FROM payment_events
            WHERE processed = false
            ORDER BY received_at ASC
            LIMIT :limit
            """)
    List<PaymentEvent> findPendingEventsForProcessing(@Param("limit") int limit);
}
