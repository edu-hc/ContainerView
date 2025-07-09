package com.ftc.containerView.model.user;

import jakarta.validation.constraints.*;

public record UserMeDTO(
        @NotNull(message = "ID é obrigatório")
        Long id,
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
        @NotNull(message = "Função é obrigatória")
        UserRole role,
        boolean twoFactorEnabled)
{


}
