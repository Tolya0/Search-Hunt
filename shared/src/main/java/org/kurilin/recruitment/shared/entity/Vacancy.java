package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kurilin.recruitment.shared.enums.VacancyStatus;
import org.kurilin.recruitment.shared.enums.WorkFormat;

import java.time.LocalDateTime;

//@Builder
@Getter
@Setter
@Entity
@Table(name = "vacancies")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vacancy
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @Column(name = "title", nullable = false, length = 100)
        private String title;

        @NonNull
        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "department_id")
        private Department department;

        @NonNull
        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.SET_NULL)
        @JoinColumn(name = "hr_manager_id")
        private User hrManager;

        @NonNull
        @Column(name = "requirements", columnDefinition = "TEXT")
        private String requirements;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        private VacancyStatus status = VacancyStatus.OPEN;

        @CreationTimestamp
        @Column(name = "created_at", updatable = false)
        private LocalDateTime createdAt;

        @NonNull
        @Column(name = "salary_min")
        private Integer salaryMin;

        @NonNull
        @Column(name = "salary_max")
        private Integer salaryMax;

        @NonNull
        @Column(name = "description", columnDefinition = "TEXT")
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(name = "work_format", nullable = false, length = 20)
        private WorkFormat workFormat = WorkFormat.OFFICE;



    }