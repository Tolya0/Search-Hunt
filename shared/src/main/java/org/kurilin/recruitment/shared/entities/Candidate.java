package org.kurilin.recruitment.shared.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "candidates")
@NoArgsConstructor
@RequiredArgsConstructor
public class Candidate
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "experience", columnDefinition = "TEXT")
        private String experience;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @NonNull
        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "person_data_id", referencedColumnName = "id")
        private PersonData personData;
    }