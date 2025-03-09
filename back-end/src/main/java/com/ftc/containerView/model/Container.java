package com.ftc.containerView.model;
import java.time.LocalDateTime;
import java.util.Objects;


public class Container {

    private Long id;

    private User user;

    private String description;

    private String imageUrl; // Link da imagem no armazenamento externo

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
