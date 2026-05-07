package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.ApplicationDAO;
import org.kurilin.recruitment.server.dao.InterviewDAO;
import org.kurilin.recruitment.server.dao.UserDAO;
import org.kurilin.recruitment.server.service.InterviewService;
import org.kurilin.recruitment.shared.entity.Application;
import org.kurilin.recruitment.shared.entity.Interview;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import org.kurilin.recruitment.shared.enums.InterviewStatus;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.exception.ScheduleConflictException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.HrInterviewsRequestDTO;
import org.kurilin.recruitment.shared.network.dto.InterviewResponseDTO;
import org.kurilin.recruitment.shared.network.dto.InterviewScheduleRequestDTO;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class InterviewServiceImpl implements InterviewService {
    private static final Logger logger = LoggerFactory.getLogger(InterviewServiceImpl.class);
    private final InterviewDAO interviewDAO;
    private final ApplicationDAO applicationDAO;
    private final UserDAO userDAO;
    private final Gson gson = GsonFactory.getGson();

    public InterviewServiceImpl(InterviewDAO interviewDAO, ApplicationDAO applicationDAO, UserDAO userDAO) {
        this.interviewDAO = interviewDAO;
        this.applicationDAO = applicationDAO;
        this.userDAO = userDAO;
    }

    @Override
    public Response scheduleInterview(String payload) throws ScheduleConflictException, RecruitmentBusinessException {
        logger.info("Scheduling interview with payload {}", payload);

        InterviewScheduleRequestDTO dto = gson.fromJson(payload, InterviewScheduleRequestDTO.class);
        if (dto == null || dto.getHrManagerId() == null || dto.getApplicationId() == null || dto.getPlannedTime() == null || dto.getLocation() == null) {
            logger.warn("Invalid interview scheduling request: {}", payload);
            throw new RecruitmentBusinessException("Invalid request format: hrManagerId, applicationId, plannedTime and location are required");
        }

        LocalDateTime dateTime = dto.getPlannedTime();
        if (dateTime.isBefore(LocalDateTime.now())) {
            logger.warn("Attempt to schedule interview in the past: {}", dateTime);
            throw new ScheduleConflictException("Cannot schedule interview in the past");
        }
        if (interviewDAO.hasOverlappingInterviews(dto.getHrManagerId(), dateTime)) {
            logger.warn("Schedule conflict for HR Manager id: {} at time: {}", dto.getHrManagerId(), dateTime);
            throw new ScheduleConflictException("HR Manager has another interview scheduled at this time");
        }

        Optional<Application> applicationOpt = applicationDAO.findById(Application.class, dto.getApplicationId());
        if (applicationOpt.isEmpty()) {
            logger.warn("Application not found for id: {}", dto.getApplicationId());
            throw new RecruitmentBusinessException("Application not found");
        }
        if (applicationOpt.get().getStatus() == ApplicationStatus.REJECTED || applicationOpt.get().getStatus() == ApplicationStatus.HIRED) {
            throw new RecruitmentBusinessException("Cannot schedule interview for closed applications");
        }

        Optional<User> hrManagerOpt = userDAO.findById(User.class, dto.getHrManagerId());
        if (hrManagerOpt.isEmpty()) {
            logger.warn("HR Manager not found for id: {}", dto.getHrManagerId());
            throw new RecruitmentBusinessException("HR Manager not found");
        }

        Interview interview = Interview.builder()
                .application(applicationOpt.get())
                .scheduledDate(dateTime)
                .status(InterviewStatus.SCHEDULED)
                .location(dto.getLocation())
                .build();

        interviewDAO.save(interview);
        logger.info("Interview scheduled successfully for application id: {} at time: {}", dto.getApplicationId(), dateTime);

        applicationOpt.get().setStatus(ApplicationStatus.INTERVIEW_SCHEDULED);
        applicationDAO.update(applicationOpt.get());
        logger.info("Application status updated to INTERVIEW_SCHEDULED for application id: {}", dto.getApplicationId());

        return new Response(true, "Interview scheduled successfully", null);
    }

    @Override
    public Response getHrInterviews(String payload) throws RecruitmentBusinessException {
        logger.info("Get HR interviews request");

        HrInterviewsRequestDTO dto = gson.fromJson(payload, HrInterviewsRequestDTO.class);
        if (dto == null || dto.getHrManagerId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: hrManagerId is required");
        }
        List<Interview> interviews = interviewDAO.findByHrManagerId(dto.getHrManagerId());
        List<InterviewResponseDTO> responseDTO = interviews.stream()
                .map(interview -> InterviewResponseDTO.builder()
                        .interviewId(interview.getId())
                        .applicationId(interview.getApplication().getId())
                        .candidateName(interview.getApplication().getCandidate().getPersonData().getFullName())
                        .vacancyTitle(interview.getApplication().getVacancy().getTitle())
                        .scheduledTime(interview.getScheduledDate())
                        .status(interview.getStatus())
                        .location(interview.getLocation())
                        .build())
                .toList();
        logger.info("HR interviews found: {}", responseDTO.size());
        return new Response(true, "HR interviews retrieved successfully", gson.toJson(responseDTO));
    }
}
