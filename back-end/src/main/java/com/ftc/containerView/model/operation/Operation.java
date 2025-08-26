package com.ftc.containerView.model.operation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ftc.containerView.model.container.Container;
import com.ftc.containerView.model.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "operations")
@EntityListeners(AuditingEntityListener.class)
@Builder
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "operation", cascade = CascadeType.PERSIST)
    @JoinColumn(name = "container_id", unique = true, nullable = false)
    private List<Container> containers = new ArrayList<>(); //Container container;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ctv", nullable = false)
    private String ctv;

    @Column(name = "exporter", nullable = false)
    private String exporter;

    @Column(name = "ship", nullable = false)
    private String ship;

    @Column(name = "terminal", nullable = false)
    private String terminal;

    @Column(name = "deadline_draft", nullable = false)
    private Date deadlineDraft;

    @Column(name = "destination", nullable = false)
    private String destination;

    @Column(name = "arrival_date", nullable = false)
    private Date arrivalDate;

    @Column(name = "reservation", nullable = false)
    private String reservation;

    @Column(name = "ref_client", nullable = false)
    private String refClient;

    @Column(name = "load_deadline", nullable = false)
    private String loadDeadline;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by_cpf")
    private String updatedByCpf;

    public Operation() {
    }

    public Operation(OperationDTO operationDTO, User user) {
        this.ctv = operationDTO.ctv();
        this.exporter = operationDTO.exporter();
        this.ship = operationDTO.ship();
        this.terminal = operationDTO.terminal();
        this.deadlineDraft = operationDTO.deadlineDraft();
        this.destination = operationDTO.destination();
        this.arrivalDate = operationDTO.arrivalDate();
        this.reservation = operationDTO.reservation();
        this.refClient = operationDTO.refClient();
        this.loadDeadline = operationDTO.loadDeadline();
        this.user = user;
    }
}