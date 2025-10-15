package com.ftc.containerView.infra.security.auth.totp;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Service
@RequiredArgsConstructor
public class TotpService {

    private static final Logger logger = LoggerFactory.getLogger(TotpService.class);

    @Value("${app.name:ContainerView}")
    private String appName;

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeVerifier verifier = new DefaultCodeVerifier(new DefaultCodeGenerator(), timeProvider);

    /**
     * Gera um novo secret TOTP (Base32, 32 caracteres)
     * Este secret deve ser armazenado no banco de dados
     */
    public String generateSecret() {
        String secret = secretGenerator.generate();
        logger.info("Novo secret TOTP gerado com sucesso");
        return secret;
    }

    /**
     * Gera QR Code em formato Data URI (Base64) para ser exibido no frontend
     *
     * @param secret Secret TOTP do usuário
     * @param userIdentifier Identificador do usuário (email ou CPF)
     * @return String no formato data:image/png;base64,...
     */
    public String generateQrCodeDataUri(String secret, String userIdentifier) {
        try {
            QrData data = new QrData.Builder()
                    .label(userIdentifier)
                    .secret(secret)
                    .issuer(appName)
                    .algorithm(HashingAlgorithm.SHA1) // SHA1 é o padrão, máxima compatibilidade
                    .digits(6) // 6 dígitos (padrão do Google Authenticator)
                    .period(30) // 30 segundos (padrão)
                    .build();

            byte[] imageData = qrGenerator.generate(data);
            String mimeType = qrGenerator.getImageMimeType();
            String dataUri = getDataUriForImage(imageData, mimeType);

            logger.info("QR Code gerado com sucesso para usuário: {}", userIdentifier);
            return dataUri;

        } catch (QrGenerationException e) {
            logger.error("Erro ao gerar QR Code para usuário: {}. Erro: {}", userIdentifier, e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar QR Code", e);
        }
    }

    /**
     * Verifica se o código TOTP fornecido é válido
     *
     * @param secret Secret TOTP do usuário
     * @param code Código de 6 dígitos fornecido pelo usuário
     * @return true se válido, false caso contrário
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || secret.isBlank()) {
            logger.warn("Tentativa de verificação com secret nulo ou vazio");
            return false;
        }

        if (code == null || code.isBlank() || code.length() != 6) {
            logger.warn("Código TOTP inválido: formato incorreto");
            return false;
        }

        try {
            boolean isValid = verifier.isValidCode(secret, code);
            logger.info("Verificação TOTP: {}", isValid ? "SUCESSO" : "FALHA");
            return isValid;
        } catch (Exception e) {
            logger.error("Erro ao verificar código TOTP. Erro: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Gera código TOTP atual (usado apenas para testes/debugging)
     * NÃO USE EM PRODUÇÃO - o código deve vir do Authenticator do usuário
     */
    public String getCurrentCode(String secret) {
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        try {
            long currentBucket = Math.floorDiv(timeProvider.getTime(), 30);
            return codeGenerator.generate(secret, currentBucket);
        } catch (Exception e) {
            logger.error("Erro ao gerar código TOTP. Erro: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao gerar código TOTP", e);
        }
    }
}