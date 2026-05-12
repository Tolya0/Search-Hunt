package org.kurilin.recruitment.server.core;

import org.kurilin.recruitment.server.dao.*;
import org.kurilin.recruitment.server.dao.impl.*;
import org.kurilin.recruitment.server.dispatcher.RequestDispatcher;
import org.kurilin.recruitment.server.exception.RecruitmentServerException;
import org.kurilin.recruitment.server.service.*;
import org.kurilin.recruitment.server.service.impl.*;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int PORT = 1024;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port {}", PORT);

            RequestDispatcher globalDispatcher = getGlobalDispatcher();

            Runtime.getRuntime()
                    .addShutdownHook(new Thread(() -> {
                logger.info("Shutting down server");
                HibernateSessionFactory.shutdown();
            }));

            //noinspection InfiniteLoopStatement
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Client connected: {}", socket.getRemoteSocketAddress());

                ClientHandler clientHandler = new ClientHandler(socket, globalDispatcher);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            logger.error("Error starting server", e);
            throw new RecruitmentServerException("Error starting server", e);
        }
    }

    private static RequestDispatcher getGlobalDispatcher() {
        UserDAO userDAO = new UserDAOImpl();
        CandidateDAO candidateDAO = new CandidateDAOImpl();
        ApplicationDAO applicationDAO = new ApplicationDAOImpl();
        VacancyDAO vacancyDAO = new VacancyDAOImpl();
        DepartmentDAO departmentDAO = new DepartmentDAOImpl();
        InterviewDAO interviewDAO = new InterviewDAOImpl();
        PersonDataDAO personDataDAO = new PersonDataDAOImpl();
        SourceDAO sourceDAO = new SourceDAOImpl();
        EvaluationDAO evaluationDAO = new EvaluationDAOImpl();

        UserService userService = new UserServiceImpl(userDAO, personDataDAO);
        CandidateService candidateService = new CandidateServiceImpl(candidateDAO, userService, personDataDAO);
        ApplicationService applicationService = new ApplicationServiceImpl(applicationDAO, candidateDAO, vacancyDAO, sourceDAO, evaluationDAO, interviewDAO);
        VacancyService vacancyService = new VacancyServiceImpl(vacancyDAO, departmentDAO, userDAO, applicationDAO);
        InterviewService interviewService = new InterviewServiceImpl(interviewDAO, applicationDAO, userDAO);
        SourceService sourceService = new SourceServiceImpl(sourceDAO);
        EvaluationService evaluationService = new EvaluationServiceImpl(evaluationDAO, interviewDAO, userDAO);

        userService.createDefaultAdminIfNotExists();

        return new RequestDispatcher(userService,
                candidateService,
                applicationService,
                vacancyService,
                interviewService,
                sourceService,
                evaluationService);
    }
}
