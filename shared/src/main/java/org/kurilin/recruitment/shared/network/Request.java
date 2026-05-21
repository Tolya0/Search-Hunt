package org.kurilin.recruitment.shared.network;

import lombok.*;
import org.kurilin.recruitment.shared.enums.RequestType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private RequestType type;
    private String payload;
}
