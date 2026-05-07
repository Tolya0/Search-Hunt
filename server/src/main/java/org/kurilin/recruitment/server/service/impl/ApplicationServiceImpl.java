package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.*;
import org.kurilin.recruitment.server.service.ApplicationService;
import org.kurilin.recruitment.shared.entity.*;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;


public class ApplicationServiceImpl implements ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationServiceImpl.class);
    private final ApplicationDAO applicationDAO;
    private final CandidateDAO candidateDAO;
    private final VacancyDAO vacancyDAO;
    private final SourceDAO sourceDAO;
    private final EvaluationDAO evaluationDAO;
    private final Gson gson = GsonFactory.getGson();

    public ApplicationServiceImpl(ApplicationDAO applicationDAO,
                                  CandidateDAO candidateDAO,
                                  VacancyDAO vacancyDAO,
                                  SourceDAO sourceDAO,
                                  EvaluationDAO evaluationDAO) {
        this.applicationDAO = applicationDAO;
        this.candidateDAO = candidateDAO;
        this.vacancyDAO = vacancyDAO;
        this.sourceDAO = sourceDAO;
        this.evaluationDAO = evaluationDAO;
    }

    @Override
    public Response updateApplicationStatus(String payload) throws RecruitmentBusinessException {
        logger.info("Update application status request: {}", payload);

        ApplicationStatusUpdateRequestDTO dto = gson.fromJson(payload, ApplicationStatusUpdateRequestDTO.class);
        if (dto == null || dto.getApplicationId() == null || dto.getNewStatus() == null) {
            throw new RecruitmentBusinessException("Invalid request format: application's Id and new status are required");
        }

        Optional<Application> applicationOpt = applicationDAO.findById(Application.class, dto.getApplicationId());
        if (applicationOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Application not found");
        }

        applicationOpt.get().setStatus(dto.getNewStatus());
        applicationDAO.update(applicationOpt.get());

        logger.info("Application status updated successfully for application id: {}", dto.getApplicationId());

        return new Response(true, "Application status updated successfully", null);
    }

    @Override
    public Response calculateScoring(String payload) throws RecruitmentBusinessException {
        logger.info("Calculate scoring for application request: {}", payload);

        ScoringRequestDTO dto = gson.fromJson(payload, ScoringRequestDTO.class);
        if (dto == null || dto.getApplicationId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: application's Id is required");
        }

        Optional<Application> applicationOpt = applicationDAO.findById(Application.class, dto.getApplicationId());
        if (applicationOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Application not found");
        }

        Application application = applicationOpt.get();
        Candidate candidate = application.getCandidate();
        Vacancy vacancy = application.getVacancy();

        double totalScore = 0.0;
        StringBuilder details = new StringBuilder();

        Integer expectedSalary = candidate.getExpectedSalary();
        Integer maxSalary = vacancy.getSalaryMax();

        if (expectedSalary != null && maxSalary != null) {
            if (expectedSalary <= maxSalary) {
                totalScore += 30;
                details.append("Salary: In budget (30/30).\n");
            } else {
                details.append("Salary: Out of budget (0/30).\n");
            }
        } else {
            details.append("Salary: Not specified.\n");
        }

        String req = vacancy.getRequirements();
        String skills = candidate.getSkills();

        if (req != null && skills != null) {
            String cleanReq = req.toLowerCase().replaceAll("[^a-zа-я0-9\\s#+\\-.]", " ");
            String cleanSkills = skills.toLowerCase().replaceAll("[^a-zа-я0-9\\s#+\\-.]", " ");

            Set<String> reqWords = new HashSet<>(Arrays.asList(cleanReq.strip().split("\\s+")));
            Set<String> skillWords = new HashSet<>(Arrays.asList(cleanSkills.strip().split("\\s+")));

            reqWords.remove("");
            skillWords.remove("");

            int totalWords = reqWords.size();
            int matchedWords = 0;

            for (String word : skillWords) {
                if (reqWords.contains(word)) {
                    matchedWords++;
                }
            }

            double skillsScore = 0;
            if (totalWords > 0) {
                skillsScore = ((double) matchedWords / totalWords) * 70.0;
                totalScore += skillsScore;
            }
            details.append(String.format("Skills: Match %d out of %d words (%.1f/70).\n", matchedWords, totalWords, skillsScore));
        } else {
            details.append("Skills: Not specified.\n");
        }

        ScoringResponseDTO responseDTO = ScoringResponseDTO.builder()
                .applicationId(application.getId())
                .matchPercentage(Math.round(totalScore * 100.0) / 100.0)
                .matchDetails(details.toString())
                .build();
        logger.info("Scoring calculated for Application ID {}: {}%", application.getId(), responseDTO.getMatchPercentage());
        return new Response(true, "Scoring calculated successfully", gson.toJson(responseDTO));
    }

    @Override
    public Response getApplicationsByVacancy(String payload) throws RecruitmentBusinessException {
        logger.info("Get applications by vacancy request: {}", payload);

        VacancyApplicationsRequestDTO dto = gson.fromJson(payload, VacancyApplicationsRequestDTO.class);
        if (dto == null || dto.getVacancyId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: vacancy's Id is required");
        }

        Optional<Vacancy> vacancyOpt = vacancyDAO.findById(Vacancy.class, dto.getVacancyId());
        if (vacancyOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Vacancy not found");
        }

        List<Application> applications = applicationDAO.findByVacancyId(dto.getVacancyId());
        List<ApplicationResponseDTO> responseDTO = applications.stream()
                .map(application -> ApplicationResponseDTO.builder()
                        .applicationId(application.getId())
                        .candidateName(application.getCandidate().getPersonData().getFullName())
                        .status(application.getStatus())
                        .appliedAt(application.getAppliedAt())
                        .sourceName(application.getSource() != null ? application.getSource().getName() : "Unknown")
                        .build())
                .toList();
        logger.info("Applications found: {}", responseDTO.size());
        return new Response(true, "Applications retrieved successfully", gson.toJson(responseDTO));
    }

    @Override
    public Response applyForVacancy(String payload) throws RecruitmentBusinessException {
        logger.info("Apply for vacancy request: {}", payload);
        ApplicationCreateRequestDTO dto = gson.fromJson(payload, ApplicationCreateRequestDTO.class);
        if (dto == null || dto.getCandidateId() == null || dto.getVacancyId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: candidate's Id and vacancy's Id are required");
        }
        Optional<Candidate> candidateOpt = candidateDAO.findById(Candidate.class, dto.getCandidateId());
        if (candidateOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Candidate not found");
        }
        Optional<Vacancy> vacancyOpt = vacancyDAO.findById(Vacancy.class, dto.getVacancyId());
        if (vacancyOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Vacancy not found");
        }
        Optional<Source> sourceOpt = sourceDAO.findById(Source.class, dto.getSourceId());
        Application application = Application.builder()
                .candidate(candidateOpt.get())
                .vacancy(vacancyOpt.get())
                .status(ApplicationStatus.NEW)
                .source(sourceOpt.orElse(null))
                .build();
        applicationDAO.save(application);
        logger.info("Application created successfully for candidate id: {}", dto.getCandidateId());
        return new Response(true, "Application created successfully", null);
    }

    @Override
    public Response aggregateEvaluations(String payload) throws RecruitmentBusinessException {
        logger.info("Aggregate evaluations request: {}", payload);
        AggregationRequestDTO dto = gson.fromJson(payload, AggregationRequestDTO.class);
        if (dto == null || dto.getApplicationId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: application's Id is required");
        }
        Optional<Application> applicationOpt = applicationDAO.findById(Application.class, dto.getApplicationId());
        if (applicationOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Application not found");
        }
        Application application = applicationOpt.get();

        Set<Interview> interviews = application.getInterviews();
        if (interviews.isEmpty()) {
            logger.warn("No interviews found for application id: {}", dto.getApplicationId());
            return new Response(true, "No evaluations found", gson.toJson(new AggregationResponseDTO(0.0, 0, false)));
        }
        int totalScore = 0;
        int evaluationsCount = 0;
        boolean isOverallPassed = true;
        for (Interview interview : interviews) {
            Optional<Evaluation> evaluationOpt = evaluationDAO.findByInterviewId(interview.getId());
            if (evaluationOpt.isPresent()) {
                totalScore += evaluationOpt.get().getScore();
                evaluationsCount++;

                if (!evaluationOpt.get().getIsPassed()) {
                    isOverallPassed = false;
                }
            }
        }
        double averageScore = 0.0;
        if (evaluationsCount > 0) {
            averageScore = (double) totalScore / evaluationsCount;
            averageScore = Math.round(averageScore * 10.0) / 10.0;
        } else {
            isOverallPassed = false;
        }

        AggregationResponseDTO responseDTO = AggregationResponseDTO.builder()
                .averageScore(averageScore)
                .totalEvaluations(evaluationsCount)
                .isOverallPassed(isOverallPassed)
                .build();

        logger.info("Aggregated evaluations for application id: {}: {}", dto.getApplicationId(), responseDTO);
        return new Response(true, "Evaluations aggregated successfully", gson.toJson(responseDTO));
    }

    @Override
    public Response generateOffer(String payload) throws RecruitmentBusinessException {
        logger.info("Generate offer request: {}", payload);

        OfferRequestDTO dto = gson.fromJson(payload, OfferRequestDTO.class);
        if (dto == null || dto.getApplicationId() == null || dto.getFinalSalary() == null) {
            throw new RecruitmentBusinessException("Invalid request format: application's Id and final salary are required");
        }
        Application application = applicationDAO.findById(Application.class, dto.getApplicationId())
                .orElseThrow(() -> new RecruitmentBusinessException("Application not found"));
        if (application.getStatus() == ApplicationStatus.REJECTED) {
            throw new RecruitmentBusinessException("Cannot generate offer for rejected application");
        }

        String candidateName = application.getCandidate().getPersonData().getFullName();
        String vacancyTitle = application.getVacancy().getTitle();
        String departmentName = application.getVacancy().getDepartment().getName();
        String workFormat = application.getVacancy().getWorkFormat().toString();

        String offerText = String.format(
                "Dear %s!\n\n" +
                        "We are pleased to offer you the position of %s in our %s department. \n" +
                        "Your total salary is calculated at: %d. \n" +
                        "Work format: %s.\n\n" +
                        "We look forward to welcoming you to our team! \nSincerely,\nThe HR Department",
                candidateName, vacancyTitle, departmentName, dto.getFinalSalary(), workFormat
        );
        application.setStatus(ApplicationStatus.OFFERED);
        application.setOfferText(offerText);
        applicationDAO.update(application);

        OfferResponseDTO responseDTO = OfferResponseDTO.builder()
                .offerText(offerText)
                .build();
        logger.info("Offer generated successfully for application id: {}", dto.getApplicationId());
        return new Response(true, "Offer generated successfully", gson.toJson(responseDTO));
    }

    @Override
    public Response getMyApplications(String payload) throws RecruitmentBusinessException {
        logger.info("Get my applications request: {}", payload);
        MyApplicationsRequestDTO dto = gson.fromJson(payload, MyApplicationsRequestDTO.class);
        if (dto == null || dto.getCandidateId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: candidate's Id is required");
        }
        List<Application> applications = applicationDAO.findByCandidateId(dto.getCandidateId());
        List<MyApplicationResponseDTO> responseDTOList = applications.stream()
                .map(application -> {
                    String safeOfferText;
                    if (application.getStatus() == ApplicationStatus.OFFERED ||
                        application.getStatus() == ApplicationStatus.HIRED) {
                        safeOfferText = application.getOfferText();
                    } else {
                        safeOfferText = "No offer yet.";
                    }
                    return MyApplicationResponseDTO.builder()
                            .applicationId(application.getId())
                            .vacancyTitle(application.getVacancy().getTitle())
                            .departmentName(application.getVacancy().getDepartment().getName())
                            .status(application.getStatus())
                            .appliedAt(application.getAppliedAt())
                            .offerText(safeOfferText)
                            .build();
                })
                .toList();
        logger.info("Applications found: {} for candidate: {}", responseDTOList.size(), dto.getCandidateId());
        return new Response(true, "Applications retrieved successfully", gson.toJson(responseDTOList));
    }

    @Override
    public Response generateFunnelReport(String payload) throws RecruitmentBusinessException {
        logger.info("Generate funnel report request: {}", payload);

        FunnelReportRequestDTO dto = gson.fromJson(payload, FunnelReportRequestDTO.class);
        if (dto == null || dto.getVacancyId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: vacancy's Id is required");
        }
        Optional<Vacancy> vacancyOpt = vacancyDAO.findById(Vacancy.class, dto.getVacancyId());
        if (vacancyOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Vacancy not found");
        }
        Map<ApplicationStatus, Long> funnelData = applicationDAO.getHiringFunnelForVacancy(dto.getVacancyId());
        if (funnelData == null || funnelData.isEmpty()) {
            logger.warn("No funnel data found for vacancy id: {}", dto.getVacancyId());
            return new Response(true, "No funnel data found", gson.toJson(new FunnelReportResponseDTO(new HashMap<>())));
        }

        FunnelReportResponseDTO responseDTO = FunnelReportResponseDTO.builder()
                .funnelData(funnelData)
                .build();
        logger.info("Funnel report generated successfully for vacancy id: {}", dto.getVacancyId());
        return new Response(true, "Funnel report generated successfully", gson.toJson(responseDTO));
    }

    @Override
    public Response generateSourcesReport(String payload) throws RecruitmentBusinessException {
        logger.info("Generate sources report request: {}", payload);

        Map<String, Long> sourceStats = applicationDAO.getSourceEffectivenessReport();
        if (sourceStats == null || sourceStats.isEmpty()) {
            logger.warn("No source stats found");
            return new Response(true, "No source stats found", gson.toJson(new SourcesReportResponseDTO(new HashMap<>())));
        }
        SourcesReportResponseDTO responseDTO = SourcesReportResponseDTO.builder()
                .sourceStats(sourceStats)
                .build();
        logger.info("Sources report generated successfully");
        return new Response(true, "Sources report generated successfully", gson.toJson(responseDTO));
    }
}
