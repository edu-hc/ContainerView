package com.ftc.containerView.model.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ftc.containerView.model.operation.Operation;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "cpf", unique = true, nullable = false)
    private String cpf;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role; // Ex: "USER", "ADMIN"

    @Column(name = "two_factor_enabled", nullable = false)
    private boolean twoFactorEnabled;

    @Column(name = "totp_secret", length = 64)
    private String totpSecret;

    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Operation> operations = new ArrayList<>();

    public User(String firstName, String lastName, String cpf, String email, String password, UserRole role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.role = role;
        this.twoFactorEnabled = false;
    }

    public User(String firstName, String lastName, String cpf, String email, String password, UserRole role, boolean twoFactorEnabled) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.cpf = cpf;
        this.email = email;
        this.password = password;
        this.role = role;
        this.twoFactorEnabled = twoFactorEnabled;
    }

    public void addOperation(Operation operation) {
        operations.add(operation);
        operation.setUser(this);
    }
}



