package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyApplicationResponseDTO {
    private Long applicationId;
    private String vacancyTitle;
    private String departmentName;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;
    private String offerText;
}
