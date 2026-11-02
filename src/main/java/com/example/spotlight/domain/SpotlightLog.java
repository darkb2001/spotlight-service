package com.example.spotlight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SPOTLIGHT_LOG")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotlightLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "OWNER_ID", nullable = false)
    private Long ownerId;

    @Column(name = "RESOURCE_CODE", nullable = false, length = 50)
    private String resourceCode;

    @Column(name = "HIGHLIGHTS", length = 1000)
    private String highlights;

    @Column(name = "POSITION")
    private Integer position;

    @Column(name = "APPLIED_DATE")
    private LocalDateTime appliedDate;

    @PrePersist
    protected void onCreate() {
        appliedDate = LocalDateTime.now();
    }
}
