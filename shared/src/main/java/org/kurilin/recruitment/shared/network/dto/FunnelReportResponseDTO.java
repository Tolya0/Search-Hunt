package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunnelReportResponseDTO {
    private Map<ApplicationStatus, Long> funnelData;
}
