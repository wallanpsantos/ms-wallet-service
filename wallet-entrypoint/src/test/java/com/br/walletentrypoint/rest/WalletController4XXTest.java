package com.br.walletentrypoint.rest;

import com.br.walletcore.domain.Money;
import com.br.walletentrypoint.exceptions.ValidationExceptionHandler;
import com.br.walletentrypoint.rest.facade.WalletFacade;
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

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes de CLIENT ERROR (4XX) para WalletController
 * Valida os cenários onde há erros do cliente (dados inválidos, regras de negócio, etc.)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Controller - Client Error Tests (4XX)")
class WalletController4XXTest {

    private static final String BASE_PATH = "/api/v1/wallets";

    @Mock
    private WalletFacade walletFacade;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    void setUp() {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(walletController)
                .setControllerAdvice(new ValidationExceptionHandler())
                .build();
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @DisplayName("POST /wallets - Should return 400 when userId is blank")
    void shouldReturn400WhenUserIdIsBlank() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "",
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Validation Failed"))
                .body("details", hasSize(1))
                .body("details[0].field", equalTo("userId"))
                .body("details[0].message", equalTo("User ID is required"));
    }

    @Test
    @DisplayName("POST /wallets - Should return 400 when currency is invalid")
    void shouldReturn400WhenCurrencyIsInvalid() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "688c2e05c0514a144d4bd13c",
                            "currency": "INVALID"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Validation Failed"))
                .body("details", hasSize(1))
                .body("details[0].field", equalTo("currency"))
                .body("details[0].message", equalTo("Currency must be 3 uppercase letters"));
    }

    @Test
    @DisplayName("POST /wallets - Should return 400 with multiple validation errors")
    void shouldReturn400WithMultipleValidationErrors() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "",
                            "currency": "br"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Validation Failed"))
                .body("details", hasSize(2))
                .body("details.field", hasItem("userId"))
                .body("details.field", hasItem("currency"));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should return 400 when amount is zero")
    void shouldReturn400WhenDepositAmountIsZero() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "amount": 0,
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/{userId}/deposit", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Validation Failed"))
                .body("details[0].field", equalTo("amount"))
                .body("details[0].message", equalTo("Amount must be greater than zero"));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/withdraw - Should return 400 when amount is negative")
    void shouldReturn400WhenWithdrawAmountIsNegative() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "amount": -25.50,
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/{userId}/withdraw", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Validation Failed"))
                .body("details[0].field", equalTo("amount"))
                .body("details[0].message", equalTo("Amount must be greater than zero"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should return 400 when fromUserId is blank")
    void shouldReturn400WhenTransferFromUserIdIsBlank() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "fromUserId": "",
                            "toUserId": "000022e05c0514a144d400002",
                            "amount": 50.00,
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Validation Failed"))
                .body("details[0].field", equalTo("fromUserId"))
                .body("details[0].message", equalTo("Source user ID is required"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should return 400 when date format is invalid")
    void shouldReturn400WhenHistoricalBalanceDateIsInvalid() {
        given()
                .queryParam("date", "invalid-date")
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Invalid Parameter Type"));
    }

    @Test
    @DisplayName("POST /wallets - Should return 400 when JSON is malformed")
    void shouldReturn400WhenJsonIsMalformed() {
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "688c2e05c0514a144d4bd13c"
                            "currency": "BRL"
                        }
                        """) // JSON inválido (falta vírgula)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Invalid Request Body"))
                .body("details[0].field", equalTo("requestBody"))
                .body("details[0].message", equalTo("Invalid JSON format"));
    }

    // ==================== BUSINESS RULE VIOLATIONS (400) ====================

    @Test
    @DisplayName("POST /wallets - Should return 400 when user already has wallet")
    void shouldReturn400WhenUserAlreadyHasWallet() {
        // Given
        when(walletFacade.createWallet(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("User already has a wallet"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "688c2e05c0514a144d4bd13c",
                            "currency": "BRL"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("User already has a wallet"))
                .body("details[0].field", equalTo("businessRule"))
                .body("details[0].message", equalTo("User already has a wallet"));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should return 400 when wallet not found")
    void shouldReturn400WhenWalletNotFoundForDeposit() {
        // Given
        String userId = "688c999999999999999999999";
        when(walletFacade.deposit(anyString(), any(Money.class)))
                .thenThrow(new IllegalArgumentException("Wallet not found for user: " + userId));

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
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("Wallet not found for user: " + userId));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/withdraw - Should return 400 when insufficient funds")
    void shouldReturn400WhenInsufficientFunds() {
        // Given
        when(walletFacade.withdraw(anyString(), any(Money.class)))
                .thenThrow(new IllegalArgumentException("Insufficient funds"));

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
                .post(BASE_PATH + "/{userId}/withdraw", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("Insufficient funds"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should return 400 when currency mismatch")
    void shouldReturn400WhenTransferCurrencyMismatch() {
        // Given
        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenThrow(new IllegalArgumentException("Currency mismatch between wallets"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "fromUserId": "688c2e05c0514a144d4bd13c",
                            "toUserId": "000022e05c0514a144d400002",
                            "amount": 50.00,
                            "currency": "USD"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("Currency mismatch between wallets"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should return 400 when trying to transfer to same user")
    void shouldReturn400WhenTransferringToSameUser() {
        // Given
        String userId = "688c2e05c0514a144d4bd13c";
        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenThrow(new IllegalArgumentException("Cannot transfer to same user"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "fromUserId": "%s",
                            "toUserId": "%s",
                            "amount": 50.00,
                            "currency": "BRL"
                        }
                        """.formatted(userId, userId))
                .when()
                .post(BASE_PATH + "/transfer")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("Cannot transfer to same user"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should return 400 when date is in future")
    void shouldReturn400WhenHistoricalDateIsInFuture() {
        // Given
        LocalDate futureDate = LocalDate.now().plusDays(1);
        when(walletFacade.getHistoricalBalance(anyString(), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Date cannot be in the future"));

        // When & Then
        given()
                .queryParam("date", futureDate.toString())
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("Date cannot be in the future"));
    }

    @Test
    @DisplayName("GET /wallets/{userId} - Should return 400 when wallet not found")
    void shouldReturn400WhenWalletNotFoundForGet() {
        // Given
        String nonexistentUserId = UUID.randomUUID().toString();
        when(walletFacade.getWallet(anyString()))
                .thenThrow(new IllegalArgumentException("Wallet not found for user: " + nonexistentUserId));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}", nonexistentUserId)
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Business Rule Violation"))
                .body("message", equalTo("Wallet not found for user: " + nonexistentUserId));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should return 400 when required parameter is missing")
    void shouldReturn400WhenRequiredParameterIsMissing() {
        // Given - Nenhum parâmetro 'date' é fornecido na requisição

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("statusCode", equalTo(400))
                .body("title", equalTo("Missing Required Parameter"))
                .body("message", equalTo("One or more required parameters are missing"))
                .body("details[0].field", equalTo("date"))
                .body("details[0].message", equalTo("Required parameter 'date' of type 'LocalDate' is missing"));
    }
}