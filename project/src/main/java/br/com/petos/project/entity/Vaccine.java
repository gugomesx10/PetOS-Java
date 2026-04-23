package br.com.petos.project.entity;

import br.com.petos.project.enums.VaccineStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vaccines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vaccine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "application_date")
    private LocalDate applicationDate;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private VaccineStatus status = VaccineStatus.PENDING;
}

