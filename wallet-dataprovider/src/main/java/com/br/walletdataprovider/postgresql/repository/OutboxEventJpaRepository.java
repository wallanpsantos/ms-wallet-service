package com.br.walletdataprovider.postgresql.repository;

import com.br.walletdataprovider.postgresql.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, String> {


    @Query("SELECT o FROM OutboxEventEntity o WHERE o.processed = false AND o.retryCount < 3")
    List<OutboxEventEntity> findUnprocessedEvents();
}
