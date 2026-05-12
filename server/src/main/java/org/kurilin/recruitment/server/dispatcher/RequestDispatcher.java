package org.kurilin.recruitment.server.dispatcher;

import org.kurilin.recruitment.server.service.*;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestDispatcher {
    private static final Logger logger = LoggerFactory.getLogger(RequestDispatcher.class);
    private final UserService userService;
    private final CandidateService candidateService;
    private final ApplicationService applicationService;
    private final VacancyService vacancyService;
    private final InterviewService interviewService;
    private final SourceService sourceService;
    private final EvaluationService evaluationService;

    public RequestDispatcher(UserService userService,
                             CandidateService candidateService,
                             ApplicationService applicationService,
                             VacancyService vacancyService,
                             InterviewService interviewService,
                             SourceService sourceService,
                             EvaluationService evaluationService) {
        this.userService = userService;
        this.candidateService = candidateService;
        this.applicationService = applicationService;
        this.vacancyService = vacancyService;
        this.interviewService = interviewService;
        this.sourceService = sourceService;
        this.evaluationService = evaluationService;
    }

    public Response dispatch(Request request) throws RecruitmentBusinessException {

        if (request == null || request.getType() == null) {
            logger.warn("Received invalid request: type is null");
            return new Response(false, "Invalid request format: Unknown RequestType", null);
        }
        logger.info("Dispatching request of type: {}", request.getType());

        return switch (request.getType()) {
            case LOGIN -> {
                logger.info("Received login request");
                yield userService.login(request.getPayload());
            }
            case GET_EVALUATION -> {
                logger.info("Received get evaluation request");
                yield evaluationService.getEvaluation(request.getPayload());
            }
            case SEARCH_CANDIDATES -> {
                logger.info("Received search candidates request");
                yield candidateService.searchCandidates(request.getPayload());
            }
            case UPDATE_APPLICATION_STATUS -> {
                logger.info("Received update application status request");
                yield applicationService.updateApplicationStatus(request.getPayload());
            }
            case GET_VACANCY_APPLICATIONS -> {
                logger.info("Received get vacancy applications request");
                yield applicationService.getApplicationsByVacancy(request.getPayload());
            }
            case REGISTER_CANDIDATE -> {
                logger.info("Received register candidate request");
                yield candidateService.registerCandidate(request.getPayload());
            }
            case UPDATE_PROFILE -> {
                logger.info("Received update profile request");
                yield candidateService.updateCandidate(request.getPayload());
            }
            case CHANGE_PASSWORD -> {
                logger.info("Received change password request");
                yield userService.changePassword(request.getPayload());
            }
            case GET_OPEN_VACANCIES -> {
                logger.info("Received get open vacancies request");
                yield vacancyService.getOpenVacancies(request.getPayload());
            }
            case GET_ALL_SOURCES -> {
                logger.info("Received get all sources request");
                yield sourceService.getAllSources();
            }
            case GET_ALL_DEPARTMENTS -> {
                logger.info("Received get all departments request");
                yield vacancyService.getAllDepartments(request.getPayload());
            }
            case GET_HR_INTERVIEWS -> {
                logger.info("Received get HR interviews request");
                yield interviewService.getHrInterviews(request.getPayload());
            }
            case APPLY_FOR_VACANCY -> {
                logger.info("Received apply for vacancy request");
                yield applicationService.applyForVacancy(request.getPayload());
            }
            case CALCULATE_SCORING -> {
                logger.info("Received calculate scoring request");
                yield applicationService.calculateScoring(request.getPayload());
            }
            case SCHEDULE_INTERVIEW -> {
                logger.info("Received schedule interview request");
                yield interviewService.scheduleInterview(request.getPayload());
            }
            case ADD_EVALUATION -> {
                logger.info("Received add evaluation request");
                yield evaluationService.addEvaluation(request.getPayload());
            }
            case AGGREGATE_EVALUATIONS -> {
                logger.info("Received aggregate evaluations request");
                yield applicationService.aggregateEvaluations(request.getPayload());
            }
            case GENERATE_REPORT_FUNNEL -> {
                logger.info("Received generate report funnel request");
                yield applicationService.generateFunnelReport(request.getPayload());
            }
            case GENERATE_REPORT_SOURCES -> {
                logger.info("Received generate report sources request");
                yield applicationService.generateSourcesReport(request.getPayload());
            }
            case GENERATE_REPORT_TIME -> {
                logger.info("Received generate report time request");
                yield vacancyService.generateTimeReport(request.getPayload());
            }
            case GENERATE_OFFER -> {
                logger.info("Received generate offer request");
                yield applicationService.generateOffer(request.getPayload());
            }
            case GET_MY_APPLICATIONS -> {
                logger.info("Received get my applications request");
                yield applicationService.getMyApplications(request.getPayload());
            }
            case GET_CANDIDATE_PROFILE -> {
                logger.info("Received get candidate profile request");
                yield candidateService.getMyProfile(request.getPayload());
            }
            case ADD_USER -> {
                logger.info("Received add user request");
                yield userService.addUser(request.getPayload());
            }
            case UPDATE_USER -> {
                logger.info("Received update user request");
                yield userService.updateUser(request.getPayload());
            }
            case BLOCK_USER -> {
                logger.info("Received block user request");
                yield userService.blockUser(request.getPayload());
            }
            case DELETE_USER -> {
                logger.info("Received delete user request");
                yield userService.deleteUser(request.getPayload());
            }
            case GET_ALL_USERS -> {
                logger.info("Received get all users request");
                yield userService.getAllUsers(request.getPayload());
            }
            case CREATE_VACANCY -> {
                logger.info("Received create vacancy request");
                yield vacancyService.createVacancy(request.getPayload());
            }
            case UPDATE_VACANCY -> {
                logger.info("Received update vacancy request");
                yield vacancyService.updateVacancy(request.getPayload());
            }
            case SEARCH_VACANCIES -> {
                logger.info("Received search vacancies request");
                yield vacancyService.searchVacancies(request.getPayload());
            }
            case CLOSE_VACANCY -> {
                logger.info("Received close vacancy request");
                yield vacancyService.closeVacancy(request.getPayload());
            }
            default -> {
                logger.warn("Unknown request type: {}", request.getType());
                throw new RecruitmentBusinessException("Unknown request type: " + request.getType());
            }
        };

    }
}
