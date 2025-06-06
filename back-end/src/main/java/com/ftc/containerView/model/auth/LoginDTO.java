package com.ftc.containerView.model.auth;

import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.br.CPF;

public record LoginDTO(
        //@CPF
        String cpf,
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,100}$",
                message = "Senha deve ter pelo menos 8 caracteres, 1 letra e 1 número")
        String password) {
}
