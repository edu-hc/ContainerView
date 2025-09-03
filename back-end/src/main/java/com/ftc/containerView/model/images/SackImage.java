package com.ftc.containerView.model.images;

import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.operation.Operation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "sack_images")
@Builder
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class SackImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Column(name = "image_key", nullable = false)
    private String imageKey;

    public SackImage() {

    }
}