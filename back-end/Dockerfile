# Usar apenas runtime - Maven roda no CodeBuild
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023-headless

# Resolver conflito curl-minimal e instalar curl
RUN yum update -y && \
    yum install -y curl --allowerasing && \
    yum clean all

# Criar usuário
RUN adduser containerapp

WORKDIR /app

# Copiar JAR já compilado
COPY target/containerView-*.jar app.jar
RUN chown containerapp:containerapp app.jar

USER containerapp
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]