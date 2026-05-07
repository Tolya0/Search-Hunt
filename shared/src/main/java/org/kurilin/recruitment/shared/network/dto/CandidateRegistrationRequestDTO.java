package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kurilin.recruitment.shared.enums.SexType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandidateRegistrationRequestDTO {
    private String username;
    private String password;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate birthDate;
    private SexType sex;
    private Integer experience;
    private String skills;
    private Integer expectedSalary;
    private String resumeURL;
}
