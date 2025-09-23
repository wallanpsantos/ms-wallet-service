package com.br.walletdataprovider.postgresql.repository;

import com.br.walletdataprovider.postgresql.entity.WalletTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WalletTransactionJpaRepository extends JpaRepository<WalletTransactionEntity, String> {
    List<WalletTransactionEntity> findByWalletIdAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
            String walletId, LocalDateTime timestamp);
}
