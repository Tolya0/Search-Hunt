package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;

public interface CandidateService {
    Response searchCandidates(String payload) throws RecruitmentBusinessException;
    Response registerCandidate(String payload) throws RecruitmentBusinessException;
    Response updateCandidate(String payload) throws RecruitmentBusinessException;
}
