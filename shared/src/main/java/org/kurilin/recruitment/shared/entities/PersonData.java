package org.kurilin.recruitment.shared.entities;

import lombok.*;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "person_data")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class PersonData
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @Column(name = "full_name", nullable = false, unique = true, length = 80)
        private String fullName;

        @NonNull
        @Column(name = "birth_date", nullable = false)
        private LocalDate birthDate;

        @NonNull
        @Column(name = "email", nullable = false, unique = true, length = 100)
        private String email;

        @NonNull
        @Column(name = "phone", nullable = false, unique = true, length = 20)
        private String phone;

        @NonNull
        @Column(name = "sex", length = 10)
        private String sex;

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
