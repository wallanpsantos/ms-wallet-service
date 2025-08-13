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
import static org.assertj.core.api.Assertions.assertThat;
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

        var expectedResponse = new WalletResponse(
                "688c334d57bd95d223b9af9c",
                userId,
                BigDecimal.ZERO,
                currency,
                LocalDateTime.now()
        );

        when(walletFacade.createWallet(userId, currency))
                .thenReturn(expectedResponse);

        // When
        WalletResponse actualResponse = given()
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
                .extract()
                .as(WalletResponse.class);

        // Then
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(expectedResponse);

        // Validações específicas para campos gerados
        assertThat(actualResponse.id()).isNotNull();
        assertThat(actualResponse.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("GET /wallets/{userId} - Should get wallet successfully (200)")
    void shouldGetWalletSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";

        var expectedResponse = new WalletResponse(
                "688c334d57bd95d223b9af9c",
                userId,
                new BigDecimal("200.50"),
                "BRL",
                LocalDateTime.now()
        );

        when(walletFacade.getWallet(userId))
                .thenReturn(expectedResponse);

        // When
        WalletResponse actualResponse = given()
                .when()
                .get(BASE_PATH + "/{userId}", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(WalletResponse.class);

        // Then
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expectedResponse);

        assertThat(actualResponse.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should deposit successfully (200)")
    void shouldDepositSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        BigDecimal amount = new BigDecimal("200.50");

        var expectedResponse = new TransactionResponse(
                UUID.randomUUID().toString(),
                "688c334d57bd95d223b9af9c",
                "DEPOSIT",
                amount,
                "BRL",
                amount,
                LocalDateTime.now(),
                UUID.randomUUID().toString()
        );

        when(walletFacade.deposit(anyString(), any(Money.class)))
                .thenReturn(expectedResponse);

        // When
        TransactionResponse actualResponse = given()
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
                .extract()
                .as(TransactionResponse.class);

        // Then
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("id", "correlationId")
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expectedResponse);

        // Validações específicas para campos gerados
        assertThat(actualResponse.id()).isNotNull();
        assertThat(actualResponse.correlationId()).isNotNull();
        assertThat(actualResponse.timestamp()).isNotNull();
        assertThat(actualResponse.type()).isEqualTo("DEPOSIT");
    }

    @Test
    @DisplayName("POST /wallets/{userId}/withdraw - Should withdraw successfully (200)")
    void shouldWithdrawSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        BigDecimal withdrawAmount = new BigDecimal("25.50");
        BigDecimal balanceAfter = new BigDecimal("125.00");

        var expectedResponse = new TransactionResponse(
                UUID.randomUUID().toString(),
                "688c334d57bd95d223b9af9c",
                "WITHDRAW",
                withdrawAmount,
                "BRL",
                balanceAfter,
                LocalDateTime.now(),
                UUID.randomUUID().toString()
        );

        when(walletFacade.withdraw(anyString(), any(Money.class)))
                .thenReturn(expectedResponse);

        // When
        TransactionResponse actualResponse = given()
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
                .extract()
                .as(TransactionResponse.class);

        // Then
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFields("id", "correlationId")
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expectedResponse);

        // Validações específicas
        assertThat(actualResponse.id()).isNotNull();
        assertThat(actualResponse.correlationId()).isNotNull();
        assertThat(actualResponse.timestamp()).isNotNull();
        assertThat(actualResponse.type()).isEqualTo("WITHDRAW");
        assertThat(actualResponse.amount()).isEqualTo(withdrawAmount);
        assertThat(actualResponse.balanceAfter()).isEqualTo(balanceAfter);
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should transfer successfully (200)")
    void shouldTransferSuccessfully() {
        // Given
        String correlationId = UUID.randomUUID().toString();

        var expectedTransactions = List.of(
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
        );

        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenReturn(expectedTransactions);

        // When
        List<TransactionResponse> actualTransactions = given()
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
                .extract()
                .jsonPath()
                .getList(".", TransactionResponse.class);

        // Then
        assertThat(actualTransactions).hasSize(2);

        // Comparar cada elemento individualmente da posicao 0
        assertThat(actualTransactions.get(0))
                .usingRecursiveComparison()
                .ignoringFields("id", "timestamp")
                // Para qualquer campo BigDecimal, use compareTo() em vez de equals()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedTransactions.get(0));

        // Comparar cada elemento individualmente da posicao 1
        assertThat(actualTransactions.get(1))
                .usingRecursiveComparison()
                .ignoringFields("id", "timestamp")
                // Para qualquer campo BigDecimal, use compareTo() em vez de equals()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedTransactions.get(1));

        // Validações específicas para transferência
        assertThat(actualTransactions.get(0).type()).isEqualTo("TRANSFER_OUT");
        assertThat(actualTransactions.get(1).type()).isEqualTo("TRANSFER_IN");
        assertThat(actualTransactions.get(0).correlationId())
                .isEqualTo(actualTransactions.get(1).correlationId());

        // Validações de valores BigDecimal
        assertThat(actualTransactions.get(0).amount()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(actualTransactions.get(1).amount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance - Should get balance successfully (200)")
    void shouldGetBalanceSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";

        var expectedResponse = new BalanceResponse(
                userId,
                new BigDecimal("200.50"),
                "BRL",
                LocalDateTime.now()
        );

        when(walletFacade.getBalance(userId))
                .thenReturn(expectedResponse);

        // When
        BalanceResponse actualResponse = given()
                .when()
                .get(BASE_PATH + "/{userId}/balance", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .as(BalanceResponse.class);

        // Then
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(LocalDateTime.class)
                .isEqualTo(expectedResponse);

        assertThat(actualResponse.timestamp()).isNotNull();
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should get historical balance (200)")
    void shouldGetHistoricalBalanceSuccessfully() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        LocalDate date = LocalDate.of(2025, 8, 1);

        var expectedResponse = new BalanceResponse(
                userId,
                new BigDecimal("150.50"),
                "BRL",
                LocalDateTime.now()
        );

        when(walletFacade.getHistoricalBalance(userId, date))
                .thenReturn(expectedResponse);

        // When
        List<BalanceResponse> actualResponses = given()
                .queryParam("date", "2025-08-01")
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", userId)
                .then()
                .statusCode(HttpStatus.OK.value())
                .extract()
                .jsonPath()
                .getList(".", BalanceResponse.class);

        // Then
        assertThat(actualResponses).hasSize(1);

        BalanceResponse actualResponse = actualResponses.getFirst();
        assertThat(actualResponse)
                .usingRecursiveComparison()
                .ignoringFieldsOfTypes(LocalDateTime.class)
                // Para qualquer campo BigDecimal, use compareTo() em vez de equals()
                .withComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .isEqualTo(expectedResponse);
    }
}