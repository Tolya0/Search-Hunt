package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "sources")
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ToString.Include
    private Long id;

    @NonNull
    @Column(name = "name", nullable = false, unique = true, length = 50)
    @ToString.Include
    private String name;

    @OneToMany(mappedBy = "source")
    private transient Set<Application> applications = new HashSet<>();
}