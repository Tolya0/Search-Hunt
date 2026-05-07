package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.CandidateDAO;
import org.kurilin.recruitment.server.dao.EvaluationDAO;
import org.kurilin.recruitment.server.dao.InterviewDAO;
import org.kurilin.recruitment.server.dao.UserDAO;
import org.kurilin.recruitment.server.service.EvaluationService;
import org.kurilin.recruitment.shared.entity.Evaluation;
import org.kurilin.recruitment.shared.entity.Interview;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.EvaluationCreateRequestDTO;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class EvaluationServiceImpl implements EvaluationService {
    private static final Logger logger = LoggerFactory.getLogger(EvaluationServiceImpl.class);
    private final EvaluationDAO evaluationDAO;
    private final InterviewDAO interviewDAO;
    private final UserDAO userDAO;
    private final Gson gson = GsonFactory.getGson();

    public EvaluationServiceImpl(EvaluationDAO evaluationDAO, InterviewDAO interviewDAO, UserDAO userDAO) {
        this.evaluationDAO = evaluationDAO;
        this.interviewDAO = interviewDAO;
        this.userDAO = userDAO;
    }
    @Override
    public Response addEvaluation(String payload) throws RecruitmentBusinessException {
        logger.info("Add evaluation request: {}", payload);

        EvaluationCreateRequestDTO dto = gson.fromJson(payload, EvaluationCreateRequestDTO.class);
        if (dto == null || dto.getInterviewId() == null || dto.getEvaluatorId() == null || dto.getScore() == null || dto.getIsPassed() == null) {
            throw new RecruitmentBusinessException("Invalid request format: interview's id, evaluator's id, score and isPassed are required");
        }

        Optional<Interview> interviewOpt = interviewDAO.findById(Interview.class, dto.getInterviewId());
        if (interviewOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Interview with id " + dto.getInterviewId() + " not found");
        }

        Optional<User> userOpt = userDAO.findById(User.class, dto.getEvaluatorId());
        if (userOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Evaluator with id " + dto.getEvaluatorId() + " not found");
        }

        if (dto.getScore() < 0 || dto.getScore() > 10) {
            throw new RecruitmentBusinessException("Score must be between 0 and 10");
        }

        Evaluation evaluation = Evaluation.builder()
                .interview(interviewOpt.get())
                .evaluator(userOpt.get())
                .score(dto.getScore())
                .isPassed(dto.getIsPassed())
                .comments(dto.getComments())
                .build();
        evaluationDAO.save(evaluation);
        logger.info("Evaluation added successfully for interview id: {}", dto.getInterviewId());

        return new Response(true, "Evaluation added successfully", null);
    }
}
