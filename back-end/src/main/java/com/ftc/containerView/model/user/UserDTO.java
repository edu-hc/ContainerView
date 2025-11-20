package com.ftc.containerView.model.user;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.br.CPF;

public record UserDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 2, max = 50, message = "Nome deve ter entre 2 e 50 caracteres")
        String firstName,
        @NotBlank(message = "Sobrenome é obrigatório")
        @Size(min = 2, max = 50, message = "Sobrenome deve ter entre 2 e 50 caracteres")
        String lastName,
        //@CPF(message = "CPF inválido")
        String cpf,
        @Email(message = "Email inválido")
        @NotBlank(message = "Email é obrigatório")
        String email,
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,100}$",
                message = "Senha deve ter pelo menos 8 caracteres, 1 letra e 1 número")
        String password,
        @NotNull(message = "Função é obrigatória")
        UserRole role,
        @NotNull(message = "Configuração de autenticação de dois fatores é obrigatória")
        boolean twoFactorEnabled){


}
