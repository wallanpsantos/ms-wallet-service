package com.br.walletentrypoint.rest;

import com.br.walletcore.domain.Money;
import com.br.walletentrypoint.rest.facade.WalletFacade;
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

    private final WalletFacade walletFacade;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("Creating wallet for user: {}", request.userId());

        WalletResponse response = walletFacade.createWallet(request.userId(), request.currency());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable String userId) {
        log.info("Getting wallet for user: {}", userId);

        WalletResponse response = walletFacade.getWallet(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(@PathVariable String userId,
                                                       @Valid @RequestBody DepositRequest request) {
        log.info("Processing deposit for user: {}", userId);

        Money amount = Money.of(request.amount(), request.currency());
        TransactionResponse response = walletFacade.deposit(userId, amount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@PathVariable String userId,
                                                        @Valid @RequestBody WithdrawRequest request) {

        log.info("Processing withdrawal for user: {}", userId);

        Money amount = Money.of(request.amount(), request.currency());
        TransactionResponse response = walletFacade.withdraw(userId, amount);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    public ResponseEntity<List<TransactionResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Processing transfer from {} to {}", request.fromUserId(), request.toUserId());

        Money amount = Money.of(request.amount(), request.currency());
        List<TransactionResponse> response = walletFacade.transfer(request.fromUserId(), request.toUserId(), amount);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<BalanceResponse> getCurrentBalance(@PathVariable String userId) {
        log.info("Getting current balance for user: {}", userId);

        BalanceResponse response = walletFacade.getBalance(userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}/balance/historical")
    public ResponseEntity<List<BalanceResponse>> getHistoricalBalance(
            @PathVariable String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.info("Getting historical balance for user: {} at date: {}", userId, date);

        BalanceResponse response = walletFacade.getHistoricalBalance(userId, date);

        return ResponseEntity.ok(List.of(response));
    }

}