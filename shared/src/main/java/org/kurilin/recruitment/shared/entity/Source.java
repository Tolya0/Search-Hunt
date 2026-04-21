package org.kurilin.recruitment.shared.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "sources")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Source
    {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @NonNull
        @Column(name = "name", nullable = false, unique = true, length = 50)
        private String name;


    }