# ================================================================================================
# DOCKERFILE MULTI-STAGE - SOLUÇÃO GARANTIDA
# ================================================================================================

# Stage 1: Build Maven
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023 AS builder

# Instalar Maven se necessário (Amazon Linux já tem)
RUN yum update -y && yum install -y maven && yum clean all

WORKDIR /build

# Copiar arquivos Maven
COPY back-end/pom.xml .
COPY back-end/.mvn .mvn/
COPY back-end/mvnw .

# Download dependências (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copiar código fonte
COPY back-end/src/ src/

# Build do projeto
RUN ./mvnw clean package -DskipTests -B

# Verificar se JAR foi criado
RUN ls -la target/ && test -f target/containerView-*.jar

# Stage 2: Runtime
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023-headless

# Instalar curl para health check
RUN yum update -y && yum install -y curl && yum clean all

WORKDIR /app

# Copiar JAR do stage anterior
COPY --from=builder /build/target/containerView-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]