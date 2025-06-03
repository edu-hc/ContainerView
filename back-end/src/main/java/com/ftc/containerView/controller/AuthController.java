package com.ftc.containerView.controller;

import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.infra.security.auth.TempTokenService;
import com.ftc.containerView.infra.security.auth.TokenService;
import com.ftc.containerView.infra.security.auth.email.EmailService;
import com.ftc.containerView.model.auth.LoginDTO;
import com.ftc.containerView.model.auth.LoginResponseDTO;
import com.ftc.containerView.model.auth.TwoFAResponseDTO;
import com.ftc.containerView.model.auth.VerifyCodeDTO;
import com.ftc.containerView.model.user.User;
import com.ftc.containerView.model.user.UserDTO;
import com.ftc.containerView.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TempTokenService tempTokenService;
    private final EmailService emailService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login (@RequestBody LoginDTO login, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("POST /auth/login - Tentando autenticar usuário com CPF: {}. IP: {}", login.cpf(), request.getRemoteAddr());
        User user = userRepository.findByCpf(login.cpf())
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        if(passwordEncoder.matches(login.password(), user.getPassword())) {
            logger.info("Usuário autenticado com sucesso: {}", user.getCpf());
            if(user.isTwoFactorEnabled()) {
                logger.info("2FA habilitado para o usuário: {}", user.getCpf());
                String token = tempTokenService.generateTempToken(user);
                emailService.sendVerificationCode(user);
                long execTime = System.currentTimeMillis() - startTime;
                logger.info("POST /auth/login concluído para usuário: {}. Tempo de resposta: {}ms", user.getCpf(), execTime);
                return ResponseEntity.ok(new LoginResponseDTO(user.getCpf(), user.isTwoFactorEnabled(), token));
            }
            String token = tokenService.generateToken(user);
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /auth/login concluído para usuário: {}. Tempo de resposta: {}ms", user.getCpf(), execTime);
            return ResponseEntity.ok(new LoginResponseDTO(user.getCpf(), user.isTwoFactorEnabled(), token));
        }
        logger.warn("Falha na autenticação para CPF: {}", login.cpf());
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<TwoFAResponseDTO> verify(@RequestBody VerifyCodeDTO verifyCodeDTO, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("POST /auth/verify - Verificando código 2FA para token temporário. IP: {}", request.getRemoteAddr());
        String userCpf = tempTokenService.validateTempToken(verifyCodeDTO.tempToken());
        if (userCpf.isEmpty()) {
            logger.warn("Token temporário inválido na verificação 2FA");
            return ResponseEntity.badRequest().build();
        }
        if (emailService.verifyCode(userCpf, verifyCodeDTO.code())) {
            logger.info("Código 2FA válido para usuário: {}", userCpf);
            User user = userRepository.findByCpf(userCpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
            String token = tokenService.generateToken(user);
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /auth/verify concluído para usuário: {}. Tempo de resposta: {}ms", userCpf, execTime);
            return ResponseEntity.ok(new TwoFAResponseDTO(user.getCpf(), token, "authenticated"));
        }
        logger.warn("Código 2FA inválido para usuário: {}", userCpf);
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register (@RequestBody UserDTO register, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("POST /auth/register - Tentando registrar usuário com CPF: {}. IP: {}", register.cpf(), request.getRemoteAddr());
        Optional<User> user = userRepository.findByCpf(register.cpf());
        if (user.isEmpty()) {
            User newUser = new User(register.firstName(),
                    register.lastName(),
                    register.cpf(),
                    register.email(),
                    passwordEncoder.encode(register.password()),
                    register.role(),
                    register.twoFactorEnabled());
            User savedUser = userRepository.save(newUser);
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("Usuário registrado com sucesso: {}. Tempo de resposta: {}ms", savedUser.getCpf(), execTime);
            return ResponseEntity.ok(savedUser);
        }
        logger.warn("Tentativa de registro para CPF já existente: {}", register.cpf());
        return ResponseEntity.badRequest().build();
    }
}
