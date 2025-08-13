package com.br.walletentrypoint.rest;

import com.br.walletcore.domain.Money;
import com.br.walletentrypoint.rest.facade.WalletFacade;
import com.br.walletentrypoint.rest.response.BalanceResponse;
import com.br.walletentrypoint.rest.response.TransactionResponse;
import com.br.walletentrypoint.rest.response.WalletResponse;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes de SUCESSO (2XX) para WalletController
 * Valida os cenários onde as operações são executadas com sucesso
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Controller - Success Tests (2XX)")
class WalletController2XXTest {

    private static final String BASE_PATH = "/api/v1/wallets";

    @Mock
    private WalletFacade walletFacade;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    void setUp() {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(walletController).build();
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @DisplayName("POST /wallets - Should create wallet successfully (201)")
    void shouldCreateWalletSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        String currency = "BRL";

        // A configuração do mock funciona exatamente da mesma forma
        when(walletFacade.createWallet(userId, currency))
                .thenReturn(new WalletResponse(
                        "688c334d57bd95d223b9af9c",
                        userId,
                        BigDecimal.ZERO,
                        currency,
                        LocalDateTime.now()
                ));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "%s",
                            "currency": "%s"
                        }
                        """.formatted(userId, currency))
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("id", notNullValue())
                .body("userId", equalTo(userId))
                .body("balance", equalTo(0))
                .body("currency", equalTo(currency));
    }

    @Test
    @DisplayName("GET /wallets/{userId} - Should get wallet successfully (200)")
    void shouldGetWalletSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";

        when(walletFacade.getWallet(userId))
                .thenReturn(new WalletResponse(
                        "688c334d57bd95d223b9af9c",
                        userId,
                        new BigDecimal("200.50"),
                        "BRL",
                        LocalDateTime.now()
                ));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("userId", equalTo(userId))
                .body("balance", equalTo(200.5f));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should deposit successfully (200)")
    void shouldDepositSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        BigDecimal amount = new BigDecimal("200.50");

        when(walletFacade.deposit(anyString(), any(Money.class)))
                .thenReturn(new TransactionResponse(
                        "c942674d-06af-4d71-aa98-3a63ef9faaa1",
                        "688c334d57bd95d223b9af9c",
                        "DEPOSIT",
                        amount,
                        "BRL",
                        amount,
                        LocalDateTime.now(),
                        UUID.randomUUID().toString()
                ));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "amount": 200.50,
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/{userId}/deposit", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("type", equalTo("DEPOSIT"))
                .body("amount", equalTo(200.5f));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/withdraw - Should withdraw successfully (200)")
    void shouldWithdrawSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";

        when(walletFacade.withdraw(anyString(), any(Money.class)))
                .thenReturn(new TransactionResponse(
                        UUID.randomUUID().toString(),
                        "688c334d57bd95d223b9af9c",
                        "WITHDRAW",
                        new BigDecimal("25.50"),
                        "BRL",
                        new BigDecimal("125.00"),
                        LocalDateTime.now(),
                        UUID.randomUUID().toString()
                ));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "amount": 25.50,
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/{userId}/withdraw", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("type", equalTo("WITHDRAW"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should transfer successfully (200)")
    void shouldTransferSuccessfully() {
        // Given
        String correlationId = UUID.randomUUID().toString();

        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenReturn(List.of(
                        new TransactionResponse(
                                UUID.randomUUID().toString(),
                                "wallet1",
                                "TRANSFER_OUT",
                                new BigDecimal("50.00"),
                                "BRL",
                                new BigDecimal("150.50"),
                                LocalDateTime.now(),
                                correlationId
                        ),
                        new TransactionResponse(
                                UUID.randomUUID().toString(),
                                "wallet2",
                                "TRANSFER_IN",
                                new BigDecimal("50.00"),
                                "BRL",
                                new BigDecimal("50.00"),
                                LocalDateTime.now(),
                                correlationId
                        )
                ));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "fromUserId": "688c2e05c0514a144d4bd13c",
                            "toUserId": "000022e05c0514a144d400002",
                            "amount": 50.00,
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/transfer")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(2))
                .body("[0].type", equalTo("TRANSFER_OUT"))
                .body("[1].type", equalTo("TRANSFER_IN"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance - Should get balance successfully (200)")
    void shouldGetBalanceSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";

        when(walletFacade.getBalance(userId))
                .thenReturn(new BalanceResponse(
                        userId,
                        new BigDecimal("200.50"),
                        "BRL",
                        LocalDateTime.now()
                ));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}/balance", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("userId", equalTo(userId))
                .body("balance", equalTo(200.5f));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should get historical balance (200)")
    void shouldGetHistoricalBalanceSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        LocalDate date = LocalDate.of(2025, 8, 1);

        when(walletFacade.getHistoricalBalance(userId, date))
                .thenReturn(new BalanceResponse(
                        userId,
                        new BigDecimal("150.50"),
                        "BRL",
                        LocalDateTime.now()
                ));

        // When & Then
        given()
                .queryParam("date", "2025-08-01")
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("$", hasSize(1))
                .body("[0].balance", equalTo(150.5f));
    }
}
