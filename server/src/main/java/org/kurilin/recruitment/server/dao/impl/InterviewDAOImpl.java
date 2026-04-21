package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.InterviewDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Interview;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.enums.InterviewStatus;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.OptionalInt;

public class InterviewDAOImpl extends GenericDAOImpl<Interview> implements InterviewDAO {
    private static final Logger logger = LoggerFactory.getLogger(InterviewDAOImpl.class);
    @Override
    public boolean hasOverlappingInterviews(User hrManager, LocalDateTime plannedTime) {
        try(Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            LocalDateTime startTime = plannedTime.minusHours(1);
            LocalDateTime endTime = plannedTime.plusHours(1);

            String query = "select count(i) from Interview i " +
                    "join i.application a join a.vacancy v " +
                    "where v.hrManager.id = :hrId and i.scheduledDate between :startTime and :endTime and i.status != :cancelStatus";

            Optional<Long> count = session.createQuery(query, Long.class)
                    .setParameter("hrId", hrManager.getId())
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
}
