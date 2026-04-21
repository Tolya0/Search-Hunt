package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kurilin.recruitment.shared.enums.InterviewStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "interviews")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Interview
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "application_id")
        private Application application;

        @NonNull
        @Column(name = "scheduled_date", nullable = false)
        private LocalDateTime scheduledDate;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        private InterviewStatus status = InterviewStatus.SCHEDULED;

        @NonNull
        @Column(name = "location", nullable = false, length = 255)
        private String location;

    }