package com.example.han.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscenarioModelo {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json", nullable = false)
    private String modeloJson;

    @Column(nullable = false)
    private Boolean esVersionFinal;

    @Column(nullable = false)
    private LocalDateTime fechaGuardado;

    @PrePersist
    protected void onCreate() {
        fechaGuardado = LocalDateTime.now();
        if (esVersionFinal == null) {
            esVersionFinal = false;
        }
    }
}