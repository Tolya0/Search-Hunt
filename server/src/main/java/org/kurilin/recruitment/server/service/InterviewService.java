package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.exception.ScheduleConflictException;
import org.kurilin.recruitment.shared.network.Response;

public interface InterviewService {
    Response scheduleInterview(String payload) throws ScheduleConflictException, RecruitmentBusinessException;
    Response getHrInterviews(String payload) throws RecruitmentBusinessException;
}
