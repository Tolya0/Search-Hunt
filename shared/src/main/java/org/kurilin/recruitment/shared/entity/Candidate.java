package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "candidates")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "experience")
        private Integer experience;

        @Column(name = "skills", columnDefinition = "TEXT")
        private String skills;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @NonNull
        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "person_data_id", referencedColumnName = "id")
        private PersonData personData;

        @Column(name = "resume_url", length = 255)
        private String resumeUrl;

        @Column(name = "expected_salary")
        private Integer expectedSalary;

    }