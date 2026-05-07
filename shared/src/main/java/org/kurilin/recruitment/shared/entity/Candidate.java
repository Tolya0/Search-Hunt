package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "candidates")
public class Candidate
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @ToString.Include
        private Long id;

        @Column(name = "experience")
        @ToString.Include
        private Integer experience;

        @Column(name = "skills", columnDefinition = "TEXT")
        private String skills;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        @ToString.Include
        private LocalDateTime createdAt;

        @NonNull
        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "person_data_id", referencedColumnName = "id")
        private PersonData personData;

        @Column(name = "resume_url", length = 255)
        @ToString.Include
        private String resumeUrl;

        @Column(name = "expected_salary")
        private Integer expectedSalary;

        @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
        private transient Set<Application> applications = new HashSet<>();
    }