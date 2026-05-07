package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.InterviewDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Application;
import org.kurilin.recruitment.shared.entity.Interview;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.enums.InterviewStatus;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InterviewDAOImpl extends GenericDAOImpl<Interview> implements InterviewDAO {
    private static final Logger logger = LoggerFactory.getLogger(InterviewDAOImpl.class);
    @Override
    public boolean hasOverlappingInterviews(Long hrManagerId, LocalDateTime plannedTime) {
        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            LocalDateTime startTime = plannedTime.minusHours(1);
            LocalDateTime endTime = plannedTime.plusHours(1);

            String query = "select count(i) from Interview i " +
                    "join i.application a join a.vacancy v " +
                    "where v.hrManager.id = :hrId and i.scheduledDate between :startTime and :endTime and i.status != :cancelStatus";

            Optional<Long> count = session.createQuery(query, Long.class)
                    .setParameter("hrId", hrManagerId)
                    .setParameter("startTime", startTime)
                    .setParameter("endTime", endTime)
                    .setParameter("cancelStatus", InterviewStatus.CANCELED)
                    .uniqueResultOptional();

            logger.info("Overlapping interviews found: {}", count.isPresent() && count.get() > 0);

            return count.isPresent() && count.get() > 0;
        } catch (Exception e) {
            logger.error("Error checking overlapping interviews", e);
            throw new DaoException("Error checking overlapping interviews", e);
        }
    }

    @Override
    public List<Interview> findByHrManagerId(Long hrManagerId) {
        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Interview i where i.status != :cancelStatus and i.application.vacancy.hrManager.id = :hrId order by i.scheduledDate asc";

            List<Interview> interviews = session.createQuery(query, Interview.class)
                    .setParameter("hrId", hrManagerId)
                    .setParameter("cancelStatus", InterviewStatus.CANCELED)
                    .getResultList();

            logger.info("Interviews found: {}", interviews.size());
            return interviews;
        } catch (Exception e) {
            logger.error("Error finding interviews by HR Manager id: {}", hrManagerId, e);
            throw new DaoException("Error finding interviews by HR Manager id: " + hrManagerId, e);
        }
    }
}
