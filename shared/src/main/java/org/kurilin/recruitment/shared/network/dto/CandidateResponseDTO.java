package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateResponseDTO {
    private Long id;
    private String fullName;
    private Integer experience;
    private String email;
    private String skills;
    private Integer expectedSalary;
}
