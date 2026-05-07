package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kurilin.recruitment.shared.enums.WorkFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyCreateRequestDTO {
    private String title;
    private String requirements;
    private Integer salaryMin;
    private Integer salaryMax;
    private String description;
    private WorkFormat workFormat;
    private Long departmentId;
    private Long hrManagerId;
}
