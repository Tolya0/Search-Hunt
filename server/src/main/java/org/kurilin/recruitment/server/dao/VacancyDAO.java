package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Vacancy;
import java.util.List;

public interface VacancyDAO extends GenericDAO<Vacancy> {
    List<Vacancy> findOpenVacanciesByDepartmentId(Long departmentId);
    List<Vacancy> findVacanciesByCriteria(String keyword, Integer minSalary);
    List<Vacancy> findClosedVacancies();
}
