package com.br.walletentrypoint.rest;

import com.br.walletcore.domain.Money;
import com.br.walletcore.domain.Wallet;
import com.br.walletcore.domain.WalletTransaction;
import com.br.walletcore.usecase.CreateWalletUseCase;
import com.br.walletcore.usecase.DepositUseCase;
import com.br.walletcore.usecase.GetBalanceUseCase;
import com.br.walletcore.usecase.GetWalletUseCase;
import com.br.walletcore.usecase.TransferUseCase;
import com.br.walletcore.usecase.WithdrawUseCase;
import com.br.walletentrypoint.rest.mapper.WalletResponseMapper;
import com.br.walletentrypoint.rest.request.CreateWalletRequest;
import com.br.walletentrypoint.rest.request.DepositRequest;
import com.br.walletentrypoint.rest.request.TransferRequest;
import com.br.walletentrypoint.rest.request.WithdrawRequest;
import com.br.walletentrypoint.rest.response.BalanceResponse;
import com.br.walletentrypoint.rest.response.TransactionResponse;
import com.br.walletentrypoint.rest.response.WalletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final DepositUseCase depositUseCase;
    private final WithdrawUseCase withdrawUseCase;
    private final TransferUseCase transferUseCase;
    private final GetBalanceUseCase getBalanceUseCase;
    private final WalletResponseMapper responseMapper;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("Creating wallet for user: {}", request.userId());

        Wallet wallet = createWalletUseCase.createWallet(request.userId(), request.currency());
        WalletResponse response = responseMapper.toWalletResponse(wallet);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String userId) {
        log.info("Getting wallet for user: {}", userId);

        Wallet wallet = getWalletUseCase.getWalletByUserId(userId);
        WalletResponse response = responseMapper.toWalletResponse(wallet);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable String userId,
            @Valid @RequestBody DepositRequest request) {

        log.info("Processing deposit for user: {}", userId);

        Money amount = Money.of(request.amount(), request.currency());
        WalletTransaction transaction = depositUseCase.deposit(userId, amount);
        TransactionResponse response = responseMapper.toTransactionResponse(transaction);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable String userId,
            @Valid @RequestBody WithdrawRequest request) {

        log.info("Processing withdrawal for user: {}", userId);

        Money amount = Money.of(request.amount(), request.currency());
        WalletTransaction transaction = withdrawUseCase.withdraw(userId, amount);
        TransactionResponse response = responseMapper.toTransactionResponse(transaction);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Processing transfer from {} to {}", request.fromUserId(), request.toUserId());

        Money amount = Money.of(request.amount(), request.currency());
        List<WalletTransaction> transactions = transferUseCase.transfer(
                request.fromUserId(),
                request.toUserId(),
                amount
        );
        List<TransactionResponse> response = responseMapper.toTransactionResponseList(transactions);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceResponse> getCurrentBalance(@PathVariable String userId) {
        log.info("Getting current balance for user: {}", userId);

        Money balance = getBalanceUseCase.getCurrentBalance(userId);
        BalanceResponse response = responseMapper.toBalanceResponse(userId, balance);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}/balance/historical")
    public ResponseEntity<List<BalanceResponse>> getHistoricalBalance(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Getting historical balance for user: {} at date: {}", userId, date);

        Money balance = getBalanceUseCase.getHistoricalBalance(userId, date);
        BalanceResponse response = responseMapper.toBalanceResponse(userId, balance);

        return ResponseEntity.ok(List.of(response));
    }

}