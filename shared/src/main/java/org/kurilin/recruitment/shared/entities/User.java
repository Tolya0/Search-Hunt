package org.kurilin.recruitment.shared.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kurilin.recruitment.shared.enums.Role;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
public class User
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @Column(name = "username", nullable = false, unique = true, length = 50)
        private String username;

        @NonNull
        @Column(name = "password_hash", nullable = false, length = 255)
        private String password;

        @NonNull
        @Enumerated(EnumType.STRING)
        @Column(name = "role", nullable = false, length = 20)
        private Role role;

        @NonNull
        @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
        @OnDelete(action = OnDeleteAction.CASCADE)
        @JoinColumn(name = "person_data_id", referencedColumnName = "id")
        private PersonData personData;
    }
