package com.ftc.containerView.model.container;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ftc.containerView.model.operation.Operation;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "containers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id")
public class Container {

    @Id
    private String id;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @Column(name = "image_key", nullable = false)
    @CollectionTable(name = "container_images", joinColumns = @JoinColumn(name = "container_id"))
    @ElementCollection
    private List<String> images = new ArrayList<>(); // Link da imagem no armazenamento externo

    @JsonIgnore
    @OneToOne(mappedBy = "container", orphanRemoval = true, optional = false)
    private Operation operation;

    public Container(String id, String description, List<String> images) {
        this.id = id;
        this.description = description;
        this.images = images;
    }
}