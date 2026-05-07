package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Evaluation;

import java.util.Optional;

public interface EvaluationDAO extends GenericDAO<Evaluation>{
    Optional<Evaluation> findByInterviewId(Long interviewId);
}
