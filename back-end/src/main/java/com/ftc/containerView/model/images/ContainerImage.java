package com.ftc.containerView.model.images;

import com.ftc.containerView.model.container.Container;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "images")
@Builder
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class ContainerImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "container_id_def", nullable = false)
    private Container container;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContainerImageCategory category;

    public ContainerImage() {

    }
}