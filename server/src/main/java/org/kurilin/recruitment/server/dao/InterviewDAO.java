package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Interview;
import org.kurilin.recruitment.shared.entity.User;
import java.time.LocalDateTime;
import java.util.List;

public interface InterviewDAO extends GenericDAO<Interview> {
    boolean hasOverlappingInterviews(Long hrManagerId, LocalDateTime plannedTime);
    List<Interview> findByHrManagerId(Long hrManagerId);
}
