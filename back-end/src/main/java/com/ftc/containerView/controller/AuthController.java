package com.ftc.containerView.controller;

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
import lombok.RequiredArgsConstructor;
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

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TempTokenService tempTokenService;
    private final EmailService emailService;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login (@RequestBody LoginDTO login) {
        User user = userRepository.findByCpf(login.cpf())
                .orElseThrow(() -> new RuntimeException("User not found"));
        if(passwordEncoder.matches(login.password(), user.getPassword())) {

            if(user.isTwoFactorEnabled()) {
                String token = tempTokenService.generateTempToken(user);
                emailService.sendVerificationCode(user);
                return ResponseEntity.ok(new LoginResponseDTO(user.getCpf(), user.isTwoFactorEnabled(), token));
            }
            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(new LoginResponseDTO(user.getCpf(), user.isTwoFactorEnabled(), token));

        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/verify")
    public ResponseEntity<TwoFAResponseDTO> verify(@RequestBody VerifyCodeDTO verifyCodeDTO) {
        // Valida o token temporário
        String userCpf = tempTokenService.validateTempToken(verifyCodeDTO.tempToken());

        if (userCpf.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // Verifica o código
        if (emailService.verifyCode(userCpf, verifyCodeDTO.code())) {
            // Código válido, gera o token de acesso completo
            User user = userRepository.findByCpf(userCpf)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

            String token = tokenService.generateToken(user);
            return ResponseEntity.ok(new TwoFAResponseDTO(user.getCpf(), token, "authenticated"));
        }

        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register (@RequestBody UserDTO register) {
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

            return ResponseEntity.ok(savedUser);
        }
            return ResponseEntity.badRequest().build();
    }
}
