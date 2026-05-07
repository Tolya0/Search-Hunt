package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.kurilin.recruitment.shared.enums.Role;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NonNull
    @Column(name = "username", nullable = false, unique = true, length = 50)
    @ToString.Include
    @EqualsAndHashCode.Include
    private String username;

    @NonNull
    @Column(name = "password_hash", nullable = false, length = 255)
    private String password;

    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    @ToString.Include
    private Role role;

    @Column(name = "is_blocked", nullable = false)
    @Builder.Default
    private Boolean isBlocked = false;

    @NonNull
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "person_data_id", referencedColumnName = "id")
    private PersonData personData;

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL, orphanRemoval = true)
    private transient Set<Department> managedDepartments = new HashSet<>();

    @OneToMany(mappedBy = "hrManager", cascade = CascadeType.ALL, orphanRemoval = true)
    private transient Set<Vacancy> managedVacancies = new HashSet<>();

    @OneToMany(mappedBy = "evaluator", cascade = CascadeType.ALL, orphanRemoval = true)
    private transient Set<Evaluation> evaluations = new HashSet<>();


}
