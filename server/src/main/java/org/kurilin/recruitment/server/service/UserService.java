package org.kurilin.recruitment.server.service;

import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.exception.DuplicateEntityException;
import org.kurilin.recruitment.shared.exception.InvalidCredentialsException;
import org.kurilin.recruitment.shared.exception.RecruitmentBusinessException;
import org.kurilin.recruitment.shared.network.Response;

public interface UserService {
    Response login(String payload) throws InvalidCredentialsException;
    String hashPassword(String password) ;
    void createDefaultAdminIfNotExists();
    boolean isUsernameTaken(String username);
    void saveUser(User user);
    Response changePassword(String payload) throws InvalidCredentialsException;
    Response addUser(String payload) throws InvalidCredentialsException, DuplicateEntityException;
    Response updateUser(String payload) throws InvalidCredentialsException, DuplicateEntityException;
    Response blockUser(String payload) throws InvalidCredentialsException;
    Response deleteUser(String payload) throws InvalidCredentialsException;
    Response getAllUsers(String payload) throws RecruitmentBusinessException;
}
