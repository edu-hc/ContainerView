package com.ftc.containerView.model;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "containers")
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // Indica que um usuário pode ter vários contêineres
    @JoinColumn(name = "user_id", nullable = false) // Cria a chave estrangeira para o relacionamento com a tabela 'users'
    private User user;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // Link da imagem no armazenamento externo

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Container() {
        this.createdAt = LocalDateTime.now();
    }

    public Container(User user, String description, String imageUrl) {
        this.createdAt = LocalDateTime.now();
        this.user = user;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Container container = (Container) o;
        return Objects.equals(id, container.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
