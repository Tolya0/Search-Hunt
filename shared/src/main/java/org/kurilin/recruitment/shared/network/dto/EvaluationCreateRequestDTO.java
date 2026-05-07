package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationCreateRequestDTO {
    private Long interviewId;
    private Long evaluatorId;
    private Integer score;
    private String comments;
    private Boolean isPassed;
}
