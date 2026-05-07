package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregationResponseDTO {
    private Double averageScore;
    private Integer totalEvaluations;
    private Boolean isOverallPassed;
}
