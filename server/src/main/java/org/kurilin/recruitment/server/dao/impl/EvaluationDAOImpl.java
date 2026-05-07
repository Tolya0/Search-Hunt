package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.EvaluationDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Evaluation;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class EvaluationDAOImpl extends GenericDAOImpl<Evaluation> implements EvaluationDAO {
    private static final Logger logger = LoggerFactory.getLogger(EvaluationDAOImpl.class);
    @Override
    public Optional<Evaluation> findByInterviewId(Long interviewId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Evaluation e where e.interview.id = :interviewId";

            Optional<Evaluation> evaluation = session.createQuery(query, Evaluation.class)
                    .setParameter("interviewId", interviewId)
                    .uniqueResultOptional();
            logger.info("Evaluation found: {}", evaluation.isPresent());
            return evaluation;
        } catch (Exception e) {
            logger.error("Error finding evaluation by interview id: {}", interviewId, e);
            throw new DaoException("Error finding evaluation by interview id: " + interviewId, e);
        }
    }
}
