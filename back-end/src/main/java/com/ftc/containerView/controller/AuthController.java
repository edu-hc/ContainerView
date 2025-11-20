package com.ftc.containerView.controller;

import com.ftc.containerView.infra.errorhandling.exceptions.UserNotFoundException;
import com.ftc.containerView.infra.security.auth.TempTokenService;
import com.ftc.containerView.infra.security.auth.TokenService;
import com.ftc.containerView.infra.security.auth.UserContextService;
import com.ftc.containerView.infra.security.auth.totp.TotpService;
import com.ftc.containerView.model.auth.*;
import com.ftc.containerView.model.user.*;
import com.ftc.containerView.repositories.UserRepository;
import com.ftc.containerView.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TempTokenService tempTokenService;
    private final TotpService totpService; // NOVO: Substituiu EmailService
    private final TokenService tokenService;
    private final UserContextService userContextService;
    private final UserService userService;

    /**
     * Login com suporte a 2FA via TOTP
     */
    @PostMapping("/login")
    public ResponseEntity login(@Valid @RequestBody LoginDTO login, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("POST /auth/login - Tentando autenticar usuário com CPF: {}. IP: {}", login.cpf(), request.getRemoteAddr());

        User user = userRepository.findByCpf(login.cpf())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (passwordEncoder.matches(login.password(), user.getPassword())) {
            logger.info("Usuário autenticado com sucesso: {}", user.getCpf());

            // Se 2FA está habilitado, retorna token temporário
            if (user.isTwoFactorEnabled()) {
                logger.info("2FA habilitado para o usuário: {}", user.getCpf());

                // Verifica se usuário já configurou TOTP
                if (user.getTotpSecret() == null || user.getTotpSecret().isBlank()) {
                    logger.warn("Usuário {} tem 2FA habilitado mas não configurou TOTP ainda", user.getCpf());
                    return ResponseEntity.badRequest()
                            .body("2FA habilitado mas não configurado. Configure primeiro em /auth/2fa/setup");
                }

                String tempToken = tempTokenService.generateTempToken(user);
                long execTime = System.currentTimeMillis() - startTime;
                logger.info("POST /auth/login concluído (2FA pendente) para usuário: {}. Tempo: {}ms", user.getCpf(), execTime);

                return ResponseEntity.ok(new Login2FAResponseDTO(user.getCpf(), true, tempToken));
            }

            // Login sem 2FA
            String token = tokenService.generateToken(user);
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /auth/login concluído para usuário: {}. Tempo: {}ms", user.getCpf(), execTime);

            return ResponseEntity.ok(new LoginResponseDTO(user.getCpf(), false, token));
        }

        logger.warn("Falha na autenticação para CPF: {}", login.cpf());
        return ResponseEntity.status(401).body("Credenciais inválidas");
    }

    /**
     * Verifica código TOTP e completa autenticação
     */
    @PostMapping("/verify")
    public ResponseEntity<TwoFAResponseDTO> verify(@Valid @RequestBody VerifyTotpDTO verifyTotpDTO, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("POST /auth/verify - Verificando código TOTP. IP: {}", request.getRemoteAddr());

        String userCpf = userContextService.getCurrentUser().getCpf();
        if (userCpf.isEmpty()) {
            logger.warn("Token temporário inválido na verificação TOTP");
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByCpf(userCpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        // Verifica se usuário tem TOTP configurado
        if (user.getTotpSecret() == null || user.getTotpSecret().isBlank()) {
            logger.error("Usuário {} não tem TOTP secret configurado", userCpf);
            return ResponseEntity.badRequest().build();
        }

        // Verifica código TOTP
        if (totpService.verifyCode(user.getTotpSecret(), verifyTotpDTO.code())) {
            logger.info("Código TOTP válido para usuário: {}", userCpf);

            String token = tokenService.generateToken(user);
            long execTime = System.currentTimeMillis() - startTime;
            logger.info("POST /auth/verify concluído para usuário: {}. Tempo: {}ms", userCpf, execTime);

            return ResponseEntity.ok(new TwoFAResponseDTO(user.getCpf(), token, "authenticated"));
        }

        logger.warn("Código TOTP inválido para usuário: {}", userCpf);
        return ResponseEntity.status(401).body(new TwoFAResponseDTO(userCpf, null, "invalid_code"));
    }

    /**
     * Setup inicial do TOTP - gera QR code
     * Requer autenticação (usuário já logado)
     */
    @PostMapping("/2fa/setup")
    public ResponseEntity<TotpSetupResponseDTO> setup2FA(HttpServletRequest request) {
        logger.info("POST /auth/2fa/setup - Configurando TOTP. IP: {}", request.getRemoteAddr());

        String userCpf = userContextService.getCurrentUser().getCpf();
        if (userCpf.isEmpty()) {
            logger.warn("Tentativa de setup 2FA sem autenticação");
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByCpf(userCpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        // Gera novo secret TOTP
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        user.setTwoFactorEnabled(true); // Habilita 2FA automaticamente
        userRepository.save(user);

        // Gera QR Code
        String qrCodeDataUri = totpService.generateQrCodeDataUri(secret, user.getEmail());

        logger.info("TOTP configurado com sucesso para usuário: {}", userCpf);

        return ResponseEntity.ok(new TotpSetupResponseDTO(
                secret,
                qrCodeDataUri,
                "Escaneie o QR Code com seu aplicativo Authenticator"
        ));
    }

    @PostMapping("/2fa/setup/{userCpf}")
    public ResponseEntity<TotpSetupResponseDTO> setupUser2FA(@PathVariable String userCpf, HttpServletRequest request) {
        logger.info("POST /auth/2fa/setup - Configurando TOTP. IP: {}", request.getRemoteAddr());

        User user = userRepository.findByCpf(userCpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        // Gera novo secret TOTP
        String secret = totpService.generateSecret();
        user.setTotpSecret(secret);
        user.setTwoFactorEnabled(true); // Habilita 2FA automaticamente
        userRepository.save(user);

        // Gera QR Code
        String qrCodeDataUri = totpService.generateQrCodeDataUri(secret, user.getEmail());

        logger.info("TOTP configurado com sucesso para usuário: {}", userCpf);

        return ResponseEntity.ok(new TotpSetupResponseDTO(
                secret,
                qrCodeDataUri,
                "Escaneie o QR Code com seu aplicativo Authenticator"
        ));
    }

    /**
     * Desabilita 2FA (requer código TOTP atual para segurança)
     */
    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2FA(@Valid @RequestBody VerifyTotpDTO verifyTotpDTO, HttpServletRequest request) {
        logger.info("POST /auth/2fa/disable - Desabilitando TOTP. IP: {}", request.getRemoteAddr());

        String userCpf = userContextService.getCurrentUser().getCpf();
        if (userCpf.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByCpf(userCpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        // Verifica código TOTP antes de desabilitar (segurança)
        if (!totpService.verifyCode(user.getTotpSecret(), verifyTotpDTO.code())) {
            logger.warn("Tentativa de desabilitar 2FA com código inválido para usuário: {}", userCpf);
            return ResponseEntity.status(401).body("Código TOTP inválido");
        }

        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null); // Remove secret por segurança
        userRepository.save(user);

        logger.info("2FA desabilitado com sucesso para usuário: {}", userCpf);
        return ResponseEntity.ok("2FA desabilitado com sucesso");
    }

    @PostMapping("/2fa/disable/{userCpf}")
    public ResponseEntity<?> disableUser2FA(@PathVariable String userCpf, HttpServletRequest request) {
        logger.info("POST /auth/2fa/disable - Desabilitando TOTP. IP: {}", request.getRemoteAddr());

        User user = userRepository.findByCpf(userCpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        user.setTwoFactorEnabled(false);
        user.setTotpSecret(null); // Remove secret por segurança
        userRepository.save(user);

        logger.info("2FA desabilitado com sucesso para usuário: {}", userCpf);
        return ResponseEntity.ok("2FA desabilitado com sucesso");
    }

    /**
     * Registro de novo usuário
     */
    @PostMapping("/register")
    public ResponseEntity register(@Valid @RequestBody UserDTO register, HttpServletRequest request) {
        long startTime = System.currentTimeMillis();
        logger.info("POST /auth/register - Registrando usuário com CPF: {}. IP: {}", register.cpf(), request.getRemoteAddr());

        Optional<User> existingUser = userRepository.findByCpf(register.cpf());
        if (existingUser.isPresent()) {
            logger.warn("Tentativa de registro com CPF já existente: {}", register.cpf());
            return ResponseEntity.badRequest().body("CPF já cadastrado");
        }

        User newUser = new User(
                register.firstName(),
                register.lastName(),
                register.cpf(),
                register.email(),
                passwordEncoder.encode(register.password()),
                register.role(),
                register.twoFactorEnabled()
        );

        String totpSecret = null;
        String qrCodeDataUri = null;

        if (register.twoFactorEnabled()) {
            totpSecret = totpService.generateSecret();
            newUser.setTotpSecret(totpSecret);
            qrCodeDataUri = totpService.generateQrCodeDataUri(totpSecret, newUser.getEmail());
            logger.info("TOTP gerado automaticamente para novo usuário: {}", register.cpf());
        }

        User savedUser = userService.registerUser(newUser);
        long execTime = System.currentTimeMillis() - startTime;
        logger.info("Usuário registrado com sucesso: {}. Tempo: {}ms", savedUser.getCpf(), execTime);

        if (register.twoFactorEnabled()) {
            return ResponseEntity.ok(new UserRegistration2faResponseDTO(
                    "Usuário registrado com sucesso",
                    savedUser.getCpf(),
                    totpSecret,
                    qrCodeDataUri
            ));
        }

        return ResponseEntity.ok(new UserRegistrationResponseDTO("Usuário registrado com sucesso", savedUser.getCpf()));
    }

    /**
     * Endpoint para obter informações do usuário logado
     */
    @GetMapping("/me")
    public ResponseEntity<UserMeDTO> me(HttpServletRequest request) {
        logger.info("GET /auth/me - Obtendo informações do usuário logado. IP: {}", request.getRemoteAddr());

        String userCpf = userContextService.getCurrentUser().getCpf();
        if (userCpf.isEmpty()) {
            return ResponseEntity.status(401).build();
        }

        User user = userRepository.findByCpf(userCpf)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado"));

        UserMeDTO userMeDTO = new UserMeDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getCpf(),
                user.getEmail(),
                user.getRole(),
                user.isTwoFactorEnabled()
        );

        return ResponseEntity.ok(userMeDTO);
    }
}