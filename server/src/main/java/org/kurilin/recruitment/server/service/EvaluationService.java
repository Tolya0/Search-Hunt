package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;

public interface EvaluationService {
    Response addEvaluation(String payload) throws RecruitmentBusinessException;
}
