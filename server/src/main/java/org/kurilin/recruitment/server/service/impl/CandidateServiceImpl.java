package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.CandidateDAO;
import org.kurilin.recruitment.server.dao.PersonDataDAO;
import org.kurilin.recruitment.server.service.CandidateService;
import org.kurilin.recruitment.server.service.UserService;
import org.kurilin.recruitment.shared.entity.Candidate;
import org.kurilin.recruitment.shared.entity.PersonData;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.enums.Role;
import org.kurilin.recruitment.shared.exception.DuplicateEntityException;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public class CandidateServiceImpl implements CandidateService {
    private static final Logger logger = LoggerFactory.getLogger(CandidateServiceImpl.class);
    private final CandidateDAO candidateDAO;
    private final PersonDataDAO personDataDAO;
    private final UserService userService;
    private final Gson gson = GsonFactory.getGson();

    public CandidateServiceImpl(CandidateDAO candidateDAO, UserService userService, PersonDataDAO personDataDAO) {
        this.candidateDAO = candidateDAO;
        this.userService = userService;
        this.personDataDAO = personDataDAO;
    }

    @Override
    public Response searchCandidates(String payload) throws RecruitmentBusinessException {
        logger.info("Search candidates: {}", payload);

        CandidateSearchRequestDTO dto = gson.fromJson(payload, CandidateSearchRequestDTO.class);

        if (dto == null) {
            throw new RecruitmentBusinessException("Invalid request format: CandidateSearchRequestDTO is null");
        } else if (dto.getSkills() == null || dto.getMinExperience() == null || dto.getMinExperience() < 0) {
            throw new RecruitmentBusinessException("Invalid request format: skills and min experience are required");
        }

        List<Candidate> candidateList = candidateDAO.findCandidatesByCriteria(dto.getSkills(), dto.getMinExperience());

        List<CandidateResponseDTO> candidateResponseDTOList = candidateList.stream()
                .map(candidate -> CandidateResponseDTO.builder()
                        .id(candidate.getId())
                        .fullName(candidate.getPersonData().getFullName())
                        .email(candidate.getPersonData().getEmail())
                        .experience(candidate.getExperience())
                        .skills(candidate.getSkills())
                        .expectedSalary(candidate.getExpectedSalary())
                        .build())
                .toList();
        logger.info("Candidates found: {}", candidateResponseDTOList.size());
        return new Response(true, "Candidates found: " + candidateResponseDTOList.size(), gson.toJson(candidateResponseDTOList));
    }

    @Override
    public Response registerCandidate(String payload) throws RecruitmentBusinessException {
        logger.info("Register candidate: {}", payload);

        CandidateRegistrationRequestDTO dto = gson.fromJson(payload, CandidateRegistrationRequestDTO.class);
        if (dto == null) {
            throw new RecruitmentBusinessException("Invalid request format: CandidateRegistrationRequestDTO is null");
        } else if (dto.getUsername() == null || dto.getPassword() == null || dto.getFullName() == null || dto.getEmail() == null || dto.getPhone() == null) {
            throw new RecruitmentBusinessException("Invalid request format: username, password, full name, phone and email are required");
        }

        if (userService.isUsernameTaken(dto.getUsername())) {
            throw new DuplicateEntityException("Username is already taken");
        } else if (personDataDAO.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEntityException("Email is already taken");
        } else if (personDataDAO.findByPhone(dto.getPhone()).isPresent()) {
            throw new DuplicateEntityException("Phone is already taken");
        }
        if (dto.getBirthDate().isAfter(LocalDate.now())) {
            throw new RecruitmentBusinessException("Validation Error: Birth date cannot be in the future.");
        }


        logger.info("Creating new candidate.");
        PersonData cData = PersonData.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .birthDate(dto.getBirthDate())
                .sex(dto.getSex())
                .build();

        User user = User.builder()
                .username(dto.getUsername())
                .password(userService.hashPassword(dto.getPassword()))
                .role(Role.CANDIDATE)
                .personData(cData)
                .build();

        Candidate c = Candidate.builder()
                .experience(dto.getExperience())
                .skills(dto.getSkills())
                .expectedSalary(dto.getExpectedSalary())
                .resumeUrl(dto.getResumeURL())
                .personData(cData)
                .build();


        userService.saveUser(user);
        candidateDAO.update(c);
        logger.info("Candidate registered successfully: {}", dto.getUsername());

        return new Response(true, "Candidate registered successfully", null);
    }

    @Override
    public Response updateCandidate(String payload) throws RecruitmentBusinessException {
        logger.info("Update candidate: {}", payload);

        CandidateUpdateRequestDTO dto = gson.fromJson(payload, CandidateUpdateRequestDTO.class);
        if (dto == null || dto.getId() == null) {
            throw new RecruitmentBusinessException("Invalid request format: id is required");
        }
        Optional<Candidate> candidateOpt = candidateDAO.findById(Candidate.class, dto.getId());
        if (candidateOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Candidate not found with id: " + dto.getId());
        }

        Candidate candidate = candidateOpt.get();
        if (dto.getExperience() != null) candidate.setExperience(dto.getExperience());
        if (dto.getSkills() != null) candidate.setSkills(dto.getSkills());
        if (dto.getExpectedSalary() != null) candidate.setExpectedSalary(dto.getExpectedSalary());
        if (dto.getResumeURL() != null) candidate.setResumeUrl(dto.getResumeURL());
        if (dto.getFullName() != null) candidate.getPersonData().setFullName(dto.getFullName());
        if (dto.getPhone() != null && !dto.getPhone().equals(candidate.getPersonData().getPhone())) {
            if (personDataDAO.findByPhone(dto.getPhone()).isPresent()) {
                throw new DuplicateEntityException("Phone is already taken");
            }
            candidate.getPersonData().setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(candidate.getPersonData().getEmail())) {
            if (personDataDAO.findByEmail(dto.getEmail()).isPresent()) {
                throw new DuplicateEntityException("Email is already taken");
            }
            candidate.getPersonData().setEmail(dto.getEmail());
        }
        if (dto.getBirthDate() != null) {
            if (dto.getBirthDate().isAfter(LocalDate.now())) {
                throw new RecruitmentBusinessException("Validation Error: Birth date cannot be in the future.");
            }
            candidate.getPersonData().setBirthDate(dto.getBirthDate());
        }

        candidateDAO.update(candidate);
        logger.info("Candidate updated successfully: {}", candidate.getId());
        return new Response(true, "Profile updated successfully", null);
    }

    @Override
    public Response getMyProfile(String payload) throws RecruitmentBusinessException {
        logger.info("Get my profile: {}", payload);
        CandidateProfileRequestDTO dto = gson.fromJson(payload, CandidateProfileRequestDTO.class);
        Optional<Candidate> candidateOpt = candidateDAO.findByUserId(dto.getId());
        if (candidateOpt.isEmpty()) {
            throw new RecruitmentBusinessException("Candidate not found");
        }
        Candidate candidate = candidateOpt.get();
        CandidateResponseDTO responseDTO = CandidateResponseDTO.builder()
                .id(candidate.getId())
                .fullName(candidate.getPersonData().getFullName())
                .email(candidate.getPersonData().getEmail())
                .phone(candidate.getPersonData().getPhone())
                .birthDate(candidate.getPersonData().getBirthDate())
                .experience(candidate.getExperience())
                .skills(candidate.getSkills())
                .expectedSalary(candidate.getExpectedSalary())
                .resumeURL(candidate.getResumeUrl())
                .build();
        logger.info("Candidate profile retrieved successfully: {}", candidate.getId());
        return new Response(true, "Profile loaded.", gson.toJson(responseDTO));
    }
}
