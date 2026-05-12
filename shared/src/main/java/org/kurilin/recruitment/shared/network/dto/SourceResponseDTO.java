package org.kurilin.recruitment.shared.network.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SourceResponseDTO {
    private Long id;
    private String name;

    @Override
    public String toString() {
        return name;
    }
}
