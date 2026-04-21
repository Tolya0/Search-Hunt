package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.ApplicationDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Application;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
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
}
