package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateUpdateRequestDTO {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private LocalDate birthDate;
    private Integer experience;
    private String skills;
    private Integer expectedSalary;
    private String resumeURL;
}








