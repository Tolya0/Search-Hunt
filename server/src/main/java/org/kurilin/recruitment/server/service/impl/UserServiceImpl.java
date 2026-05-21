package org.kurilin.recruitment.server.service.impl;

import com.google.gson.Gson;
import org.kurilin.recruitment.server.dao.PersonDataDAO;
import org.kurilin.recruitment.server.dao.UserDAO;
import org.kurilin.recruitment.server.exception.RecruitmentServerException;
import org.kurilin.recruitment.server.service.UserService;
import org.kurilin.recruitment.shared.entity.PersonData;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.enums.Role;
import org.kurilin.recruitment.shared.enums.SexType;
import org.kurilin.recruitment.shared.exception.DuplicateEntityException;
import org.kurilin.recruitment.shared.exception.InvalidCredentialsException;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserDAO userDAO;
    private final PersonDataDAO personDataDAO;
    private final Gson gson = GsonFactory.getGson();

    public UserServiceImpl(UserDAO userDAO, PersonDataDAO personDataDAO) {
        this.userDAO = userDAO;
        this.personDataDAO = personDataDAO;
    }

    @Override
    public Response login(String payload) throws InvalidCredentialsException {
        logger.info("Login Request: {}", payload);
        LoginRequestDTO dto = gson.fromJson(payload, LoginRequestDTO.class);
        if (dto == null || dto.getUsername() == null || dto.getPassword() == null) {
            throw new InvalidCredentialsException("Invalid request format: username and password are required");
        }

        Optional<User> userOpt = userDAO.findUserByLogin(dto.getUsername());

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("User not found: " + dto.getUsername());
        }

        if (userOpt.get().getIsBlocked() != null && userOpt.get().getIsBlocked()) {
            logger.warn("User is blocked: {}", dto.getUsername());
            throw new InvalidCredentialsException("User is blocked: " + dto.getUsername());
        }

        if (!BCrypt.checkpw(dto.getPassword(), userOpt.get().getPassword())) {
            throw new InvalidCredentialsException("Invalid username or password");
        }

        User user = userOpt.get();
        UserResponseDTO userResponseDTO = UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();

        logger.info("Login Response: {}", gson.toJson(userResponseDTO));
        return new Response(true, "Login successful.", gson.toJson(userResponseDTO));
    }

    @Override
    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public void createDefaultAdminIfNotExists() {
        try {
            Optional<User> adminOpt = userDAO.findUserByLogin("admin");
            if (adminOpt.isEmpty()) {
                logger.info("Creating default admin");

                PersonData adminData = PersonData.builder()
                        .fullName("System Administrator")
                        .birthDate(LocalDate.of(2000, 1, 1))
                        .email("admin@system.local")
                        .phone("+000000000000")
                        .sex(SexType.UNKNOWN)
                        .build();

                User defaultAdmin = User.builder()
                        .username("admin")
                        .password(hashPassword("admin"))
                        .role(Role.ADMIN)
                        .personData(adminData)
                        .build();

                userDAO.save(defaultAdmin);
                logger.info("Created default admin");
            }
        } catch (Exception e) {
            logger.error("Error creating default admin", e);
            throw new RecruitmentServerException("Error creating default admin. Server turns off.", e);
        }
    }

    @Override
    public boolean isUsernameTaken(String username) {
        Optional<User> userOpt = userDAO.findUserByLogin(username);
        return userOpt.isPresent();
    }

    @Override
    public void saveUser(User user) {
        userDAO.save(user);
    }

    @Override
    public Response changePassword(String payload) throws InvalidCredentialsException {
        logger.info("Change password request: {}", payload);

        ChangePasswordRequestDTO dto = gson.fromJson(payload, ChangePasswordRequestDTO.class);
        if (dto == null || dto.getUserId() == null || dto.getOldPassword() == null || dto.getNewPassword() == null) {
            throw new InvalidCredentialsException("Invalid request format: userId, oldPassword and newPassword are required");
        }

        Optional<User> userOpt = userDAO.findById(User.class, dto.getUserId());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("User not found");
        }

        User user = userOpt.get();
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Old password is incorrect");
        }

        user.setPassword(hashPassword(dto.getNewPassword()));
        userDAO.update(user);
        logger.info("Password changed successfully for user id: {}", dto.getUserId());

        return new Response(true, "Password changed successfully", null);
    }

    @Override
    public Response addUser(String payload) throws InvalidCredentialsException, DuplicateEntityException {
        logger.info("Add user request: {}", payload);

        UserCreateRequestDTO dto = gson.fromJson(payload, UserCreateRequestDTO.class);
        if (dto == null) {
            throw new InvalidCredentialsException("Invalid request format: UserCreateRequestDTO is null");
        } else if (dto.getUsername() == null || dto.getPassword() == null || dto.getFullName() == null || dto.getEmail() == null || dto.getPhone() == null) {
            throw new InvalidCredentialsException("Invalid request format: username, password, full name, phone and email are required");
        }
        if (dto.getBirthDate() != null && dto.getBirthDate().isAfter(java.time.LocalDate.now())) {
            throw new InvalidCredentialsException("Validation Error: Birth date cannot be in the future.");
        }


        if (isUsernameTaken(dto.getUsername())) {
            throw new DuplicateEntityException("Username is already taken");
        } else if (personDataDAO.findByEmail(dto.getEmail()).isPresent()) {
            throw new DuplicateEntityException("Email is already taken");
        } else if (personDataDAO.findByPhone(dto.getPhone()).isPresent()) {
            throw new DuplicateEntityException("Phone is already taken");
        }

        logger.info("Creating new user.");
        PersonData cData = PersonData.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .birthDate(dto.getBirthDate())
                .sex(dto.getSex())
                .build();

        User user = User.builder()
                .username(dto.getUsername())
                .password(hashPassword(dto.getPassword()))
                .role(dto.getRole())
                .personData(cData)
                .build();


        saveUser(user);
        logger.info("User created successfully: {}", dto.getUsername());

        return new Response(true, "User created successfully", null);
    }

    @Override
    public Response updateUser(String payload) throws InvalidCredentialsException, DuplicateEntityException {
        logger.info("Update user request: {}", payload);

        UserUpdateRequestDTO dto = gson.fromJson(payload, UserUpdateRequestDTO.class);
        if (dto == null || dto.getId() == null) {
            throw new InvalidCredentialsException("Invalid request format: id is required");
        }
        Optional<User> userOpt = userDAO.findById(User.class, dto.getId());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("Candidate not found with id: " + dto.getId());
        }

        User user = userOpt.get();
        if (dto.getFullName() != null) user.getPersonData().setFullName(dto.getFullName());
        if (dto.getPhone() != null && !dto.getPhone().equals(user.getPersonData().getPhone())) {
            if (personDataDAO.findByPhone(dto.getPhone()).isPresent()) {
                throw new DuplicateEntityException("Phone is already taken");
            }
            user.getPersonData().setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null && !dto.getEmail().equals(user.getPersonData().getEmail())) {
            if (personDataDAO.findByEmail(dto.getEmail()).isPresent()) {
                throw new DuplicateEntityException("Email is already taken");
            }
            user.getPersonData().setEmail(dto.getEmail());
        }

        userDAO.update(user);
        logger.info("User updated successfully: {}", user.getId());
        return new Response(true, "Profile updated successfully", null);
    }

    @Override
    public Response blockUser(String payload) throws InvalidCredentialsException {
        logger.info("Block user request: {}", payload);

        UserBlockRequestDTO dto = gson.fromJson(payload, UserBlockRequestDTO.class);
        if (dto == null || dto.getId() == null) {
            throw new InvalidCredentialsException("Invalid request format: userId is required");
        }

        Optional<User> userOpt = userDAO.findById(User.class, dto.getId());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("User not found");
        }

        User user = userOpt.get();
        user.setIsBlocked(dto.isBlock());
        userDAO.update(user);

        if (dto.isBlock()) {
            logger.info("User blocked: {}", user.getId());
        } else {
            logger.info("User unblocked: {}", user.getId());
        }
        return new Response(true, "User " + (dto.isBlock() ? "blocked" : "unblocked") + " successfully", null);
    }

    @Override
    public Response deleteUser(String payload) throws InvalidCredentialsException {
        logger.info("Delete user request: {}", payload);

        UserDeleteRequestDTO dto = gson.fromJson(payload, UserDeleteRequestDTO.class);
        if (dto == null || dto.getId() == null) {
            throw new InvalidCredentialsException("Invalid request format: userId is required");
        }

        Optional<User> userOpt = userDAO.findById(User.class, dto.getId());
        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException("User not found");
        }

        if (userOpt.get().getRole() == Role.CANDIDATE) {
            throw new InvalidCredentialsException("Cannot delete Candidate accounts from the Employee Management panel");
        }

        userDAO.delete(userOpt.get());
        logger.info("User deleted: {}", dto.getId());
        return new Response(true, "User deleted successfully", null);
    }

    @Override
    public Response getAllUsers(String payload) throws RecruitmentBusinessException {
        logger.info("Get all users request");

        List<User> users = userDAO.findAll(User.class);
        if (users.isEmpty()) {
            throw new RecruitmentBusinessException("No users found");
        }
        List<UserResponseDTO> userResponseDTOList = users.stream()
                .filter(user -> user.getRole() != Role.CANDIDATE)
                .map(user -> UserResponseDTO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .fullName(user.getPersonData().getFullName())
                        .email(user.getPersonData().getEmail())
                        .phone(user.getPersonData().getPhone())
                        .isBlock(user.getIsBlocked())
                        .build()).
                toList();
        logger.info("Users found: {}", userResponseDTOList.size());
        return new Response(true, "Users found: " + userResponseDTOList.size(), gson.toJson(userResponseDTOList));
    }
}
