package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.ApplicationDAO;
import org.kurilin.recruitment.server.dao.DepartmentDAO;
import org.kurilin.recruitment.server.dao.UserDAO;
import org.kurilin.recruitment.server.dao.VacancyDAO;
import org.kurilin.recruitment.server.service.VacancyService;
import org.kurilin.recruitment.shared.entity.Application;
import org.kurilin.recruitment.shared.entity.Department;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.entity.Vacancy;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import org.kurilin.recruitment.shared.enums.VacancyStatus;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class VacancyServiceImpl implements VacancyService {
    private static final Logger logger = LoggerFactory.getLogger(VacancyServiceImpl.class);
    private final VacancyDAO vacancyDAO;
    private final DepartmentDAO departmentDAO;
    private final UserDAO userDao;
    private final ApplicationDAO applicationDAO;
    private final Gson gson = GsonFactory.getGson();

    public VacancyServiceImpl(VacancyDAO vacancyDAO, DepartmentDAO departmentDAO, UserDAO userDao, ApplicationDAO applicationDAO) {
        this.vacancyDAO = vacancyDAO;
        this.departmentDAO = departmentDAO;
        this.userDao = userDao;
        this.applicationDAO = applicationDAO;
    }


    @Override
    public Response createVacancy(String payload) throws RecruitmentBusinessException {
        logger.info("Create vacancy request: {}", payload);

        VacancyCreateRequestDTO dto = gson.fromJson(payload, VacancyCreateRequestDTO.class);
        if (dto == null || dto.getTitle() == null || dto.getDepartmentId() == null || dto.getHrManagerId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: title, department's Id and hrManager's Id are required");
        }

        Optional<Department> department = departmentDAO.findById(Department.class, dto.getDepartmentId());
        if (department.isEmpty()) {
            throw new RecruitmentBusinessException("Department not found with id: " + dto.getDepartmentId());
        }

        Optional<User> hr = userDao.findById(User.class, dto.getHrManagerId());
        if (hr.isEmpty()) {
            throw new RecruitmentBusinessException("HR Manager not found with id: " + dto.getHrManagerId());
        }

        Vacancy vacancy = Vacancy.builder()
                .title(dto.getTitle())
                .department(department.get())
                .hrManager(hr.get())
                .requirements(dto.getRequirements())
                .status(VacancyStatus.OPEN)
                .salaryMin(dto.getSalaryMin())
                .salaryMax(dto.getSalaryMax())
                .description(dto.getDescription())
                .workFormat(dto.getWorkFormat())
                .build();

        vacancyDAO.save(vacancy);
        logger.info("Vacancy created successfully: {}", vacancy.getTitle());
        return new Response(true, "Vacancy created successfully", null);
    }

    @Override
    public Response searchVacancies(String payload) throws RecruitmentBusinessException {
        logger.info("Search vacancies request: {}", payload);

        VacancySearchRequestDTO dto = gson.fromJson(payload, VacancySearchRequestDTO.class);
        if (dto == null) {
            throw new RecruitmentBusinessException("Invalid request format");
        }

        String keyword = dto.getKeyword() != null ? dto.getKeyword() : "";
        Integer wantedSalary = dto.getWantedSalary();

        List<Vacancy> vacancies = vacancyDAO.findVacanciesByCriteria(keyword, wantedSalary);
        List<VacancyResponseDTO> vacancyList = vacancies.stream()
                .map(vacancy -> VacancyResponseDTO.builder()
                        .id(vacancy.getId())
                        .title(vacancy.getTitle())
                        .departmentName(vacancy.getDepartment().getName())
                        .requirements(vacancy.getRequirements())
                        .salaryMin(vacancy.getSalaryMin())
                        .salaryMax(vacancy.getSalaryMax())
                        .description(vacancy.getDescription())
                        .workFormat(vacancy.getWorkFormat())
                        .build())
                .toList();
        logger.info("Vacancies found: {}", vacancyList.size());
        return new Response(true, "Vacancies found: " + vacancyList.size(), gson.toJson(vacancyList));
    }

    @Override
    public Response updateVacancy(String payload) throws RecruitmentBusinessException {
        logger.info("Update vacancy request: {}", payload);

        VacancyUpdateRequestDTO dto = gson.fromJson(payload, VacancyUpdateRequestDTO.class);
        if (dto == null || dto.getId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: id is required");
        }

        Optional<Vacancy> vacancyOpt = vacancyDAO.findById(Vacancy.class, dto.getId());
        if (vacancyOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Vacancy not found with id: " + dto.getId());
        }

        Vacancy vacancy = vacancyOpt.get();
        if (dto.getTitle() != null) vacancy.setTitle(dto.getTitle());
        if (dto.getRequirements() != null) vacancy.setRequirements(dto.getRequirements());
        if (dto.getDescription() != null) vacancy.setDescription(dto.getDescription());
        if (dto.getSalaryMin() != null) vacancy.setSalaryMin(dto.getSalaryMin());
        if (dto.getSalaryMax() != null) vacancy.setSalaryMax(dto.getSalaryMax());
        if (dto.getWorkFormat() != null) vacancy.setWorkFormat(dto.getWorkFormat());
        if (dto.getStatus() != null) vacancy.setStatus(dto.getStatus());

        vacancyDAO.update(vacancy);
        logger.info("Vacancy updated successfully: {}, status: {}", vacancy.getTitle(), vacancy.getStatus());

        return new Response(true, "Vacancy updated successfully", null);
    }

    @Override
    public Response getOpenVacancies(String payload) throws RecruitmentBusinessException {
        logger.info("Get open vacancies request");

        DepartmentVacanciesRequestDTO dto = gson.fromJson(payload, DepartmentVacanciesRequestDTO.class);
        if (dto == null || dto.getDepartmentId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: department's Id is required");
        }

        List<Vacancy> vacancies = vacancyDAO.findOpenVacanciesByDepartmentId(dto.getDepartmentId());
        List<VacancyResponseDTO> vacancyResponseDTOList = vacancies.stream()
                .map(vacancy -> VacancyResponseDTO.builder()
                        .id(vacancy.getId())
                        .title(vacancy.getTitle())
                        .departmentName(vacancy.getDepartment().getName())
                        .requirements(vacancy.getRequirements())
                        .salaryMin(vacancy.getSalaryMin())
                        .salaryMax(vacancy.getSalaryMax())
                        .description(vacancy.getDescription())
                        .workFormat(vacancy.getWorkFormat())
                        .build())
                .toList();
        logger.info("Open vacancies found: {}", vacancyResponseDTOList.size());
        return new Response(true, "Open vacancies found: " + vacancyResponseDTOList.size(), gson.toJson(vacancyResponseDTOList));
    }

    @Override
    public Response closeVacancy(String payload) throws RecruitmentBusinessException {
        logger.info("Close vacancy request: {}", payload);

        VacancyCloseRequestDTO dto = gson.fromJson(payload, VacancyCloseRequestDTO.class);
        if (dto == null || dto.getVacancyId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: vacancy's Id is required");
        }
        Vacancy vacancy = vacancyDAO.findById(Vacancy.class, dto.getVacancyId())
                .orElseThrow(() -> new RecruitmentBusinessException("Vacancy not found"));

        vacancy.setStatus(VacancyStatus.CLOSED);
        vacancy.setClosedAt(LocalDateTime.now());
        vacancyDAO.update(vacancy);

        List<Application> applications = applicationDAO.findByVacancyId(dto.getVacancyId());
        long reserveCount = 0;
        for (Application application : applications) {
            if (application.getStatus() != ApplicationStatus.HIRED && application.getStatus() != ApplicationStatus.REJECTED) {
                application.setStatus(ApplicationStatus.RESERVE);
                applicationDAO.update(application);
                reserveCount++;
            }
        }

        logger.info("Vacancy closed successfully: {}, reserves: {}", vacancy.getTitle(), reserveCount);
        return new Response(true, "Vacancy closed successfully. " + reserveCount + " candidates moved to reserve.", null);
    }

    @Override
    public Response generateTimeReport(String payload) throws RecruitmentBusinessException {
        logger.info("Generate time-to-fill report request: {}", payload);

        List<Vacancy> vacancies = vacancyDAO.findClosedVacancies();

        if (vacancies.isEmpty()) {
            logger.info("No closed vacancies found");
            return new Response(true, "No closed vacancies found", gson.toJson(new TimeReportResponseDTO(0.0)) );
        }

        double averageTimeToFill = vacancies.stream()
                .filter(v -> v.getClosedAt() != null && v.getCreatedAt() != null)
                .mapToLong(v -> {
                    long days = ChronoUnit.DAYS.between(v.getCreatedAt().toLocalDate(), v.getClosedAt().toLocalDate());
                    return (days == 0) ? 1 : days;
                })
                .average()
                .orElse(0.0);
        averageTimeToFill = Math.round(averageTimeToFill * 10.0) / 10.0;
        TimeReportResponseDTO responseDTO = TimeReportResponseDTO.builder()
                .averageTimeToFillDays(averageTimeToFill)
                .build();
        logger.info("Time-to-fill report generated successfully: {}", averageTimeToFill);
        return new Response(true, "Time-to-fill report generated successfully", gson.toJson(responseDTO));
    }
}
