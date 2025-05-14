package com.ftc.containerView.model.user;

public record UserDTO(String firstName, String lastName, String cpf, String email, String password, UserRole role, boolean twoFactorEnabled) {


}
