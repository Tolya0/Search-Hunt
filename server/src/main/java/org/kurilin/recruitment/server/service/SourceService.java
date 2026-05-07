package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;

public interface SourceService {
    Response getAllSources() throws RecruitmentBusinessException;
}
