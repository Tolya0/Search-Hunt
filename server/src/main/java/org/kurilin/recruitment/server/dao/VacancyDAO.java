package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Vacancy;
import java.util.List;

public interface VacancyDAO extends GenericDAO<Vacancy> {
    List<Vacancy> findOpenVacanciesByDepartment(Long departmentId);
}
