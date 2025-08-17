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

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes de SERVER ERROR (5XX) para WalletController
 * Foca nos erros que podem ocorrer na camada de entrada (controllers, mappers, facade)
 * Erros de infraestrutura (DB, Kafka) são testados no módulo wallet-dataprovider
 * <p>
 * Estes testes cobrem os seguintes handlers do ValidationExceptionHandler:
 * - handleRuntimeException(RuntimeException ex) -> 500
 * - handleGenericException(Exception ex) -> 500
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Wallet Controller - Server Error Tests (5XX)")
class WalletController5XXTest {

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

    // ==================== RUNTIME EXCEPTION HANDLER (500) ====================
    // Testa: handleRuntimeException(RuntimeException ex)

    @Test
    @DisplayName("POST /wallets - Should return 500 when runtime exception occurs in create")
    void shouldReturn500WhenRuntimeExceptionOccursInCreate() {
        // Given
        when(walletFacade.createWallet(anyString(), anyString()))
                .thenThrow(new RuntimeException("Unexpected error in wallet creation"));

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
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Internal server error"));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should return 500 when runtime exception occurs in deposit")
    void shouldReturn500WhenRuntimeExceptionOccursInDeposit() {
        // Given
        when(walletFacade.deposit(anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Facade layer internal processing error"));

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
                .post(BASE_PATH + "/{userId}/deposit", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Internal server error"));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/withdraw - Should return 500 when runtime exception occurs in withdraw")
    void shouldReturn500WhenRuntimeExceptionOccursInWithdraw() {
        // Given
        when(walletFacade.withdraw(anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Error in response mapping"));

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
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should return 500 when runtime exception occurs in transfer")
    void shouldReturn500WhenRuntimeExceptionOccursInTransfer() {
        // Given
        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Unexpected error during transfer processing"));

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
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("GET /wallets/{userId} - Should return 500 when runtime exception occurs in get wallet")
    void shouldReturn500WhenRuntimeExceptionOccursInGetWallet() {
        // Given
        when(walletFacade.getWallet(anyString()))
                .thenThrow(new RuntimeException("Service layer internal error"));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Internal server error"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance - Should return 500 when runtime exception occurs in get balance")
    void shouldReturn500WhenRuntimeExceptionOccursInGetBalance() {
        // Given
        when(walletFacade.getBalance(anyString()))
                .thenThrow(new RuntimeException("Balance calculation error"));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}/balance", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    // ==================== GENERIC EXCEPTION HANDLER (500) ====================
    // Testa: handleGenericException(Exception ex)

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should return 500 when generic exception occurs")
    void shouldReturn500WhenGenericExceptionOccurs() {
        // Given
        when(walletFacade.getHistoricalBalance(anyString(), any(LocalDate.class)))
                .thenThrow(new Exception("Generic checked exception"));

        // When & Then
        given()
                .queryParam("date", "2025-08-01")
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Unexpected Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Unexpected error occurred"));
    }

    @Test
    @DisplayName("POST /wallets - Should return 500 when generic checked exception occurs")
    void shouldReturn500WhenGenericCheckedExceptionOccurs() {
        // Given
        when(walletFacade.createWallet(anyString(), anyString()))
                .thenThrow(new Exception("Checked exception in wallet creation"));

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
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Unexpected Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Unexpected error occurred"));
    }

    // ==================== FACADE/MAPPER SPECIFIC ERRORS (500) ====================
    // Erros específicos da camada de entrada (entrypoint)

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should return 500 when mapper throws exception")
    void shouldReturn500WhenMapperThrowsException() {
        // Given - Simulando erro no mapeamento de response
        when(walletFacade.deposit(anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Mapping error in response conversion"));

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
                .post(BASE_PATH + "/{userId}/deposit", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("GET /wallets/{userId} - Should return 500 when facade layer fails")
    void shouldReturn500WhenFacadeLayerFails() {
        // Given - Simulando erro interno na camada facade
        when(walletFacade.getWallet(anyString()))
                .thenThrow(new RuntimeException("Internal facade processing error"));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should return 500 when controller layer fails")
    void shouldReturn500WhenControllerLayerFails() {
        // Given - Simulando falha na camada controller/entrypoint
        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Controller layer internal error"));

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
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    // ==================== ADDITIONAL EDGE CASES (500) ====================
    // Casos extremos que podem gerar erros internos

    @Test
    @DisplayName("POST /wallets/{userId}/withdraw - Should return 500 when service layer throws runtime error")
    void shouldReturn500WhenServiceLayerThrowsRuntimeError() {
        // Given - Simulando erro interno no service
        when(walletFacade.withdraw(anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Service layer critical error"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "amount": 100.00,
                            "currency": "USD"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/{userId}/withdraw", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance/historical - Should return 500 when date processing fails")
    void shouldReturn500WhenDateProcessingFails() {
        // Given - Simulando erro no processamento de data
        when(walletFacade.getHistoricalBalance(anyString(), any(LocalDate.class)))
                .thenThrow(new RuntimeException("Historical balance calculation failed"));

        // When & Then
        given()
                .queryParam("date", "2025-01-15")
                .when()
                .get(BASE_PATH + "/{userId}/balance/historical", "688c2e05c0514a144d4bd13c")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("POST /wallets/transfer - Should return 500 when transaction processing fails")
    void shouldReturn500WhenTransactionProcessingFails() {
        // Given - Simulando falha no processamento da transação
        when(walletFacade.transfer(anyString(), anyString(), any(Money.class)))
                .thenThrow(new RuntimeException("Transaction processing system error"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "fromUserId": "user123",
                            "toUserId": "user456",
                            "amount": 75.25,
                            "currency": "EUR"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/transfer")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"));
    }

    @Test
    @DisplayName("GET /wallets/{userId}/balance - Should return 500 when balance service is unavailable")
    void shouldReturn500WhenBalanceServiceIsUnavailable() {
        // Given - Simulando indisponibilidade do serviço de saldo
        when(walletFacade.getBalance(anyString()))
                .thenThrow(new RuntimeException("Balance service temporarily unavailable"));

        // When & Then
        given()
                .when()
                .get(BASE_PATH + "/{userId}/balance", "user789")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Internal Server Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Internal server error"));
    }

    // ==================== CHECKED EXCEPTIONS (500) ====================
    // Testando diferentes tipos de exceções checked

    @Test
    @DisplayName("POST /wallets - Should return 500 when IO exception occurs")
    void shouldReturn500WhenIOExceptionOccurs() {
        // Given - Simulando IOException (checked exception)
        when(walletFacade.createWallet(anyString(), anyString()))
                .thenThrow(new Exception("IO error during wallet creation"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "userId": "io-test-user",
                            "currency": "CAD"
                        }
                        """)
                .when()
                .post(BASE_PATH)
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Unexpected Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Unexpected error occurred"));
    }

    @Test
    @DisplayName("POST /wallets/{userId}/deposit - Should return 500 when serialization fails")
    void shouldReturn500WhenSerializationFails() {
        // Given - Simulando erro de serialização
        when(walletFacade.deposit(anyString(), any(Money.class)))
                .thenThrow(new Exception("Response serialization error"));

        // When & Then
        given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .body("""
                        {
                            "amount": 999.99,
                            "currency": "ARS"
                        }
                        """)
                .when()
                .post(BASE_PATH + "/{userId}/deposit", "serialization-test-user")
                .then()
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body("statusCode", equalTo(500))
                .body("title", equalTo("Unexpected Error"))
                .body("message", equalTo("An unexpected error occurred while processing the request"))
                .body("details[0].field", equalTo("system"))
                .body("details[0].message", equalTo("Unexpected error occurred"));
    }
}