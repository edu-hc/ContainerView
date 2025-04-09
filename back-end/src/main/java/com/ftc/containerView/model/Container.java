package com.ftc.containerView.model;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "containers")
public class Container {

    @Id
    private String id;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "image_url", nullable = false)
    private String imageUrl; // Link da imagem no armazenamento externo

    @JsonIgnore
    @OneToOne(mappedBy = "container")
    private Operation operation;

    public Container() {

    }

    public Container(String id, String description, String imageUrl) {
        this.id = id;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public String getId() {
        return id;
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
