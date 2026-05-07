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
    public List<Vacancy> findOpenVacanciesByDepartmentId(Long departmentId) {
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

    @Override
    public List<Vacancy> findVacanciesByCriteria(String keyword, Integer wantedSalary) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String hql = "FROM Vacancy v WHERE v.status = 'OPEN' " +
                    "AND (lower(v.title) LIKE lower(:kw) OR lower(v.requirements) LIKE lower(:kw)) " +
                    "AND v.salaryMax >= :wantedSalary";

            List<Vacancy> vacancies = session.createQuery(hql, Vacancy.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .setParameter("wantedSalary", wantedSalary != null ? wantedSalary : 0)
                    .getResultList();
            logger.info("Vacancies found: {}", vacancies.size());
            return vacancies;
        } catch (Exception e) {
            logger.error("Error finding open vacancies: {}", keyword, e);
            throw new DaoException("Error searching vacancies", e);
        }
    }

    @Override
    public List<Vacancy> findClosedVacancies() {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Vacancy v where v.status = :status";

            List<Vacancy> vacancies = session.createQuery(query, Vacancy.class)
                    .setParameter("status", VacancyStatus.CLOSED)
                    .getResultList();
            logger.info("Closed vacancies found: {}", vacancies.size());
            return vacancies;
        } catch (Exception e) {
            logger.error("Error finding closed vacancies", e);
            throw new DaoException("Error finding closed vacancies", e);
        }
    }
}
