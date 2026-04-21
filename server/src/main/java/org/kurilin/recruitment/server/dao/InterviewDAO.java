package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.Interview;
import org.kurilin.recruitment.shared.entity.User;
import java.time.LocalDateTime;

public interface InterviewDAO extends GenericDAO<Interview> {
    boolean hasOverlappingInterviews(User hrManager, LocalDateTime plannedTime);
}
