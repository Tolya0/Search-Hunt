package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Application;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import java.util.List;
import java.util.Map;

public interface ApplicationDAO extends GenericDAO<Application> {
    Map<ApplicationStatus, Long> getHiringFunnelForVacancy(Long vacancyId);
    List<Application> findByVacancyId(Long vacancyId);
    List<Application> findByCandidateId(Long candidateId);
    Map<String, Long> getSourceEffectivenessReport();
}
