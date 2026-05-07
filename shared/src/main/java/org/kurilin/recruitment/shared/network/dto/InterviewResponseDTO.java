package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kurilin.recruitment.shared.enums.InterviewStatus;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewResponseDTO {
    private Long interviewId;
    private Long applicationId;
    private String candidateName;
    private String candidateEmail;
    private String vacancyTitle;
    private LocalDateTime scheduledTime;
    private String location;
    private InterviewStatus status;
}
