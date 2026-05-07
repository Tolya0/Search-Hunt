package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;

public interface VacancyService {
    Response createVacancy(String payload) throws RecruitmentBusinessException;
    Response searchVacancies(String payload) throws RecruitmentBusinessException;
    Response updateVacancy(String payload) throws RecruitmentBusinessException;
    Response getOpenVacancies(String payload) throws RecruitmentBusinessException;
    Response closeVacancy(String payload) throws RecruitmentBusinessException;
    Response generateTimeReport(String payload) throws RecruitmentBusinessException;
}
