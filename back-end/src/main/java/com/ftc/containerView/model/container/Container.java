package com.ftc.containerView.model.container;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ftc.containerView.model.images.ContainerImage;
import com.ftc.containerView.model.operation.Operation;
import com.ftc.containerView.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "containers")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = "id")
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "container_id", nullable = false)
    private String containerId;

    @Column(name = "description", nullable = false, length = 500)
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<ContainerImage> containerImages = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @Column(name = "sacks_count")
    private int sacksCount;

    @Column(name = "tare_tons")
    private float tareTons;

    @Column(name = "tare_weight")
    private float liquidWeight;

    @Column(name = "gross_weight")
    private float grossWeight;

    @Column(name = "agency_seal")
    private String agencySeal;

    @ElementCollection
    @Column(name = "other_seals")
    private List<String> otherSeals = new ArrayList<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by_cpf")
    private String updatedByCpf;



    public Container(String containerId, String description, User user, Operation operation, int sacksCount,
                     float tareTons, float liquidWeight, float grossWeight, String agencySeal, List<String> otherSeals) {
        this.description = description;
        this.user = user;
        this.operation = operation;
        this.sacksCount = sacksCount;
        this.tareTons = tareTons;
        this.liquidWeight = liquidWeight;
        this.grossWeight = grossWeight;
        this.agencySeal = agencySeal;
        this.otherSeals = otherSeals;
        this.containerId = containerId;

    }
}