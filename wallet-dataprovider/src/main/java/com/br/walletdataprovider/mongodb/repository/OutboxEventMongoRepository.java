package com.br.walletdataprovider.mongodb.repository;

import com.br.walletdataprovider.mongodb.document.OutboxEventDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxEventMongoRepository extends MongoRepository<OutboxEventDocument, String> {

    @Query("{ 'processed': false, 'retryCount': { $lt: 3 } }")
    List<OutboxEventDocument> findUnprocessedEvents();

    @Query("{ 'processed': false, 'createdAt': { $lt: ?0 } }")
    List<OutboxEventDocument> findUnprocessedEventsOlderThan(LocalDateTime timestamp);
}