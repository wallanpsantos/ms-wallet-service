package com.br.walletdataprovider.mongodb.repository;

import com.br.walletdataprovider.mongodb.document.WalletDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WalletMongoRepository extends MongoRepository<WalletDocument, String> {
    Optional<WalletDocument> findByUserId(String userId);
}