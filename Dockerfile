# =================================================================
# Stage 1: Builder - Focado em compilar a aplicação
# =================================================================
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# --- Otimização de Cache para Multi-módulo ---
# 1. Copia o POM pai primeiro
COPY pom.xml .

# 2. Copia os POMs de cada módulo
COPY wallet-core/pom.xml ./wallet-core/
COPY wallet-dataprovider/pom.xml ./wallet-dataprovider/
COPY wallet-entrypoint/pom.xml ./wallet-entrypoint/
COPY wallet-config/pom.xml ./wallet-config/

# 3. Baixa as dependências de todos os módulos
#    Isso cria uma camada de cache robusta. O build só re-executará
#    esta etapa se um dos pom.xml for alterado.
RUN mvn dependency:go-offline -B

# 4. Agora, copia o código-fonte
COPY wallet-core ./wallet-core
COPY wallet-dataprovider ./wallet-dataprovider
COPY wallet-entrypoint ./wallet-entrypoint
COPY wallet-config ./wallet-config

# 5. Compila e empacota a aplicação, pulando os testes
#    O build será executado a partir do módulo pai, que orquestra os outros.
RUN mvn clean package -DskipTests -B

# =================================================================
# Stage 2: Runtime - Focado em rodar a aplicação
# =================================================================
FROM eclipse-temurin:21-jre-alpine

# Instalar dependências necessárias para o healthcheck
RUN apk add --no-cache curl

# Criar usuário e grupo não-root para segurança
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Mudar para o usuário não-root
USER appuser

WORKDIR /app

# --- Cópia do Artefato Específico ---
# Copia apenas o JAR executável do módulo de configuração (o principal)
COPY --from=builder /app/wallet-config/target/wallet-config.jar app.jar

# Expor a porta da aplicação
EXPOSE 8080

# Health check (seu healthcheck está ótimo, sem alterações)
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# --- Entrypoint Otimizado ---
# Executa a JVM diretamente, permitindo que ela receba sinais do SO
## Configuração de JVM otimizada para Containers
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:+AlwaysPreTouch", \
            "-XX:InitialRAMPercentage=30.0", \
            "-XX:MaxRAMPercentage=80.0", \
            "-XX:+UseG1GC", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-Djava.net.preferIPv4Stack=true", \
            "-jar", \
            "app.jar"]