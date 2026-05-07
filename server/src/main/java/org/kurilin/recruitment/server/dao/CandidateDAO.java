package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Candidate;
import java.util.List;
import java.util.Optional;

public interface CandidateDAO extends GenericDAO<Candidate> {
    List<Candidate> findCandidatesByCriteria(String skills, int minExperience);
}
