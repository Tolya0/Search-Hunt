package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;

public interface ApplicationService {
    Response updateApplicationStatus(String payload) throws RecruitmentBusinessException;
    Response calculateScoring(String payload) throws RecruitmentBusinessException;
    Response getApplicationsByVacancy(String payload) throws RecruitmentBusinessException;
    Response applyForVacancy(String payload) throws RecruitmentBusinessException;
    Response aggregateEvaluations(String payload) throws RecruitmentBusinessException;
    Response generateOffer(String payload) throws RecruitmentBusinessException;
    Response getMyApplications(String payload) throws RecruitmentBusinessException;
    Response generateFunnelReport(String payload) throws RecruitmentBusinessException;
    Response generateSourcesReport(String payload) throws RecruitmentBusinessException;
}
