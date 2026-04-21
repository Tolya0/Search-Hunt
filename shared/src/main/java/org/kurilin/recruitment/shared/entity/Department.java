package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import java.util.List;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "departments")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Department
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @Column(name = "name", nullable = false, unique = true, length = 100)
        private String name;

        @ManyToOne(fetch = FetchType.LAZY)
        @OnDelete(action = OnDeleteAction.SET_NULL)
        @JoinColumn(name = "manager_id", referencedColumnName = "id")
        private User manager;

        @OneToMany(mappedBy = "department", cascade = CascadeType.ALL, orphanRemoval = true)
        private transient List<Vacancy> vacancies = new ArrayList<>();

    }