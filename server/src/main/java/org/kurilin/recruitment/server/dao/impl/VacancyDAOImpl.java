package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.VacancyDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Vacancy;
import org.kurilin.recruitment.shared.enums.VacancyStatus;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class VacancyDAOImpl extends GenericDAOImpl<Vacancy> implements VacancyDAO {
    private static final Logger logger = LoggerFactory.getLogger(VacancyDAOImpl.class);

    @Override
    public List<Vacancy> findOpenVacanciesByDepartment(Long departmentId) {
        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Vacancy v where v.department.id = :departmentId and v.status = :status";

            List<Vacancy> vacancies = session.createQuery(query, Vacancy.class)
                    .setParameter("departmentId", departmentId)
                    .setParameter("status", VacancyStatus.OPEN)
                    .getResultList();
            logger.info("Open vacancies found: {}", vacancies.size());
            return vacancies;
        } catch (Exception e) {
            logger.error("Error finding open vacancies: {}", departmentId, e);
            throw new DaoException("Error finding open vacancies: " + departmentId, e);
        }
    }
}
