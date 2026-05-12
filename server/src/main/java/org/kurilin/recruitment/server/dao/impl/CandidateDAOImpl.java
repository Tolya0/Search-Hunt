package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.CandidateDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Candidate;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class CandidateDAOImpl extends GenericDAOImpl<Candidate> implements CandidateDAO {
    private static final Logger logger = LoggerFactory.getLogger(CandidateDAOImpl.class);

    @Override
    public List<Candidate> findCandidatesByCriteria(String skills, int minExperience) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Candidate c join fetch c.personData where lower(c.skills) like lower(:skills) and c.experience >= :minExperience";

            List<Candidate> candidates = session.createQuery(query, Candidate.class)
                    .setParameter("skills", "%" + skills + "%")
                    .setParameter("minExperience", minExperience)
                    .getResultList();
            logger.info("Candidates found: {}", candidates.size());
            return candidates;
        } catch (Exception e) {
            logger.error("Error finding candidates: {}", skills, e);
            throw new DaoException("Error finding candidates: " + skills, e);
        }
    }

    @Override
    public Optional<Candidate> findByUserId(Long userId) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Candidate c join fetch c.personData pd where pd.id = (select u.personData.id from User u where u.id = :userId)";
            return session.createQuery(query, Candidate.class)
                    .setParameter("userId", userId)
                    .uniqueResultOptional();
        } catch (Exception e) {
            logger.error("Error finding candidate by user ID: {}", userId, e);
            throw new DaoException("Error finding candidate by user ID: " + userId, e);
        }
    }

}
