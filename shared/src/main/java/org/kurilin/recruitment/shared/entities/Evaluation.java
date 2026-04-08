package org.kurilin.recruitment.shared.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "evaluations")
@NoArgsConstructor
@RequiredArgsConstructor
public class Evaluation
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @OneToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "interview_id", unique = true)
        private Interview interview;

        @NonNull
        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.SET_NULL)
        @JoinColumn(name = "evaluator_id")
        private User evaluator;

        @NonNull
        @Column(name = "score")
        private Integer score;

        @Column(name = "comments", columnDefinition = "TEXT")
        private String comments;

        @CreationTimestamp
        @Column(name = "evaluated_at", updatable = false)
        private LocalDateTime evaluatedAt;


    }