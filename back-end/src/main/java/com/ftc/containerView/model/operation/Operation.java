package com.ftc.containerView.model.operation;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operations")
@Builder
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "container_id", unique = true, nullable = false)
    private Container container;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Operation() {
        createdAt = LocalDateTime.now();
    }

    public Operation(Long id, Container container, User user) {
        this.id = id;
        this.container = container;
        this.user = user;
        createdAt = LocalDateTime.now();
    }
}