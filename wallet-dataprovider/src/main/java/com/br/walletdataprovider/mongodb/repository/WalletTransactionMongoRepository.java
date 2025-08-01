package com.br.walletdataprovider.mongodb.repository;

import com.br.walletdataprovider.mongodb.document.WalletTransactionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionMongoRepository extends MongoRepository<WalletTransactionDocument, String> {

    List<WalletTransactionDocument> findByWalletIdAndTimestampLessThanEqualOrderByTimestampAsc(
            String walletId, LocalDateTime timestamp);

}