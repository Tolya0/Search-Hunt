package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewScheduleRequestDTO {
    private Long applicationId;
    private Long hrManagerId;
    private LocalDateTime plannedTime;
    private String location;
}
