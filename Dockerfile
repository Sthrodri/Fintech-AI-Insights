# Build Stage
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar pom.xml e baixar dependências (cache layer)
COPY pom.xml .
RUN apk add --no-cache maven

# Copiar código-fonte
COPY src ./src

# Compilar e empacotar
RUN mvn clean package -DskipTests -q

# Runtime Stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar JAR do builder
COPY --from=builder /app/target/api-financeira-saas-*.jar app.jar

# Criar usuário não-root para segurança
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app

USER appuser

# Expor porta
EXPOSE 8080

# Variáveis de ambiente padrão
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"

# Entrypoint
ENTRYPOINT ["java", "-jar", "app.jar"]
