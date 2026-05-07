package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "departments")
public class Department
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @ToString.Include
        private Long id;

        @NonNull
        @Column(name = "name", nullable = false, unique = true, length = 100)
        @ToString.Include
        @EqualsAndHashCode.Include
        private String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.SET_NULL)
        @JoinColumn(name = "manager_id", referencedColumnName = "id")
        private User manager;

        @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
        private transient Set<Vacancy> vacancies = new HashSet<>();

    }