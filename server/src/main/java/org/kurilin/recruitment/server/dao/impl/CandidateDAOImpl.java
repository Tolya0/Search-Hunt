package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.CandidateDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.Candidate;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class CandidateDAOImpl extends GenericDAOImpl<Candidate> implements CandidateDAO {
    private static final Logger logger = LoggerFactory.getLogger(CandidateDAOImpl.class);

    @Override
    public List<Candidate> findCandidatesByCriteria(String skills, int minExperience) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from Candidate c where lower(c.skills) like lower(:skills) and c.experience >= :minExperience";

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
}
