FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023-headless

# Instalar apenas curl
RUN yum update -y && \
    yum install -y curl --allowerasing && \
    yum clean all

WORKDIR /app

# Copiar JAR já compilado
COPY back-end/target/containerView-*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]