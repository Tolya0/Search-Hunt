package org.kurilin.recruitment.shared.entity;

import lombok.*;
import jakarta.persistence.*;
import org.kurilin.recruitment.shared.enums.SexType;

import java.time.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "person_data")
public class PersonData
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @ToString.Include
        private Long id;

        @NonNull
        @Column(name = "full_name", nullable = false, length = 80)
        @ToString.Include
        private String fullName;

        @NonNull
        @Column(name = "birth_date", nullable = false)
        private LocalDate birthDate;

        @NonNull
        @Column(name = "email", nullable = false, unique = true, length = 100)
        @ToString.Include
        @EqualsAndHashCode.Include
        private String email;

        @NonNull
        @Column(name = "phone", nullable = false, unique = true, length = 20)
        @ToString.Include
        private String phone;

        @NonNull
        @Enumerated(EnumType.STRING)
        @Column(name = "sex", length = 10)
        private SexType sex;

//        @OneToOne(mappedBy = "personData", cascade = CascadeType.ALL, orphanRemoval = true)
//        private User user;
//
//        @OneToOne(mappedBy = "personData", cascade = CascadeType.ALL, orphanRemoval = true)
//        private Candidate candidate;

        @Transient
        public Integer getAge()
            {
                return Period.between(birthDate, LocalDate.now()).getYears();
            }
    }
