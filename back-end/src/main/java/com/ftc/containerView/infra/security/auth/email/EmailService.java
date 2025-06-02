package com.ftc.containerView.infra.security.auth.email;

import com.ftc.containerView.model.auth.VerificationCode;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.repositories.VerificationCodeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final int CODE_EXPIRATION_MINUTES = 5;

    private final JavaMailSender emailSender;
    private final VerificationCodeRepository verificationCodeRepository;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Transactional
    public void sendVerificationCode(User user) {
        logger.info("Enviando código de verificação para o usuário: {}", user.getCpf());
        try {
            // Gera um código de 6 dígitos
            String code = generateRandomCode();

            // Remove códigos anteriores
            verificationCodeRepository.deleteByUserCpf(user.getCpf());

            // Salva o novo código
            VerificationCode verificationCode = new VerificationCode(
                    user.getCpf(),
                    code,
                    CODE_EXPIRATION_MINUTES
            );
            verificationCodeRepository.save(verificationCode);

            // Envia o email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("Código de Verificação - Container View");
            message.setText(String.format(
                    "Olá %s,\n\nSeu código de verificação é: %s\n\nEste código expira em %d minutos.",
                    user.getFirstName(), code, CODE_EXPIRATION_MINUTES
            ));

            emailSender.send(message);
            logger.info("Código de verificação enviado para o e-mail: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("Erro ao enviar código de verificação para o usuário: {}. Erro: {}", user.getCpf(), e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar o código de verificação por email", e);
        }
    }

    @Transactional
    public boolean verifyCode(String userCpf, String code) {
        logger.info("Verificando código de verificação para CPF: {}", userCpf);
        try {
            return verificationCodeRepository.findFirstByUserCpfOrderByExpiryDateDesc(userCpf)
                    .map(verificationCode -> {
                        boolean isValid = verificationCode.getCode().equals(code) && !verificationCode.isExpired();
                        if (isValid) {
                            // Remove o código após o uso bem-sucedido
                            verificationCodeRepository.delete(verificationCode);
                            logger.info("Código de verificação válido para CPF: {}", userCpf);
                        } else {
                            logger.warn("Código de verificação inválido ou expirado para CPF: {}", userCpf);
                        }
                        return isValid;
                    })
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Erro ao verificar código de verificação para CPF: {}. Erro: {}", userCpf, e.getMessage(), e);
            throw e;
        }
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000); // Gera um código de 6 dígitos
        return String.valueOf(code);
    }
}