package org.kurilin.recruitment.shared.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "applications")
@NoArgsConstructor
@RequiredArgsConstructor
public class Application
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "candidate_id")
        private Candidate candidate;

        @NonNull
        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "vacancy_id")
        private Vacancy vacancy;

        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.SET_NULL)
        @JoinColumn(name = "source_id")
        private Source source;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", length = 30)
        private ApplicationStatus status = ApplicationStatus.NEW;

        @CreationTimestamp
        @Column(name = "applied_at", updatable = false)
        private LocalDateTime appliedAt;


    }