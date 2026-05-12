package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kurilin.recruitment.shared.enums.VacancyStatus;
import org.kurilin.recruitment.shared.enums.WorkFormat;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponseDTO {
    private Long id;
    private String title;
    private String departmentName;
    private String requirements;
    private String description;
    private Integer salaryMin;
    private Integer salaryMax;
    private WorkFormat workFormat;
    private VacancyStatus status;
}
