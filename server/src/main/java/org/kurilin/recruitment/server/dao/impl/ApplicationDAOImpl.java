package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.ApplicationDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Application;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationDAOImpl extends GenericDAOImpl<Application> implements ApplicationDAO {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDAOImpl.class);

    @Override
    public Map<ApplicationStatus, Long> getHiringFunnelForVacancy(Long vacancyId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "select a.status, count(a) from Application a where a.vacancy.id = :vId group by a.status";

            Map<ApplicationStatus, Long> funnel = session.createQuery(query, Object[].class)
                    .setParameter("vId", vacancyId)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toMap(row -> (ApplicationStatus) row[0], row -> (Long) row[1]));

            logger.info("Hiring funnel for vacancy {} retrieved successfully", vacancyId);
            return funnel;
        } catch (Exception e) {
            logger.error("Error retrieving hiring funnel for vacancy {}", vacancyId, e);
            throw new DaoException("Error retrieving hiring funnel for vacancy " + vacancyId, e);
        }
    }

    @Override
    public List<Application> findByVacancyId(Long vacancyId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = """
                    from Application a 
                    join fetch a.candidate c
                    join fetch c.personData 
                    left join fetch a.source
                    where a.vacancy.id = :vId""";

            List<Application> applications = session.createQuery(query, Application.class)
                    .setParameter("vId", vacancyId)
                    .getResultList();

            logger.info("Applications for vacancy {} retrieved successfully, count: {}", vacancyId, applications.size());
            return applications;
        } catch (Exception e) {
            logger.error("Error retrieving applications for vacancy {}", vacancyId, e);
            throw new DaoException("Error retrieving applications for vacancy " + vacancyId, e);
        }
    }

    @Override
    public List<Application> findByCandidateId(Long candidateId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = """
                    from Application a 
                    join fetch a.vacancy v
                    join fetch v.department
                    where a.candidate.id = :cId""";
            List<Application> applications = session.createQuery(query, Application.class)
                    .setParameter("cId", candidateId)
                    .getResultList();
            logger.info("Applications for candidate {} retrieved successfully, count: {}", candidateId, applications.size());
            return applications;
        } catch (Exception e) {
            logger.error("Error retrieving applications for candidate {}", candidateId, e);
            throw new DaoException("Error retrieving applications for candidate " + candidateId, e);
        }
    }

    @Override
    public Map<String, Long> getSourceEffectivenessReport() {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "select a.source.name, count(a) from Application a where a.source is not null group by a.source.name";

            Map<String, Long> applicationsBySource = session.createQuery(query, Object[].class)
                    .getResultList()
                    .stream()
                    .collect(Collectors.toMap(row -> (String) row[0], row -> (Long) row[1]));
            logger.info("Source effectiveness report retrieved successfully");
            return applicationsBySource;
        } catch (Exception e) {
            logger.error("Error retrieving source effectiveness report", e);
            throw new DaoException("Error retrieving source effectiveness report", e);
        }
    }

    @Override
    public boolean isCandidateAppliedToVacancy(Long candidateId, Long vacancyId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "select count(a) from Application a where a.candidate.id = :cId and a.vacancy.id = :vId";
            Long count = session.createQuery(query, Long.class)
                    .setParameter("cId", candidateId)
                    .setParameter("vId", vacancyId)
                    .uniqueResult();
            logger.info("Candidate {} applied to vacancy {}: {}", candidateId, vacancyId, count > 0);
            return count > 0;
        } catch (Exception e) {
            logger.error("Error checking if candidate {} applied to vacancy {}", candidateId, vacancyId, e);
            throw new DaoException("Error checking if candidate " + candidateId + " applied to vacancy " + vacancyId, e);
        }
    }

    @Override
    public Optional<Application> findByIdWithDetails(Long Id) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = """
                    from Application a
                    join fetch a.candidate c
                    join fetch c.personData
                    join fetch a.vacancy v
                    join fetch v.department
                    where a.id = :aId""";
            Optional<Application> application = session.createQuery(query, Application.class)
                    .setParameter("aId", Id)
                    .uniqueResultOptional();
            logger.info("Application with id {} retrieved successfully", Id);
            return application;
        } catch (Exception e) {
            logger.error("Error retrieving application with id {}", Id, e);
            throw new DaoException("Error retrieving application with id " + Id, e);
        }
    }
}
