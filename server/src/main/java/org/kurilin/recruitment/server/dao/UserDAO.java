package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.User;

import java.util.Optional;

public interface UserDAO extends GenericDAO<User>{
    Optional<User> findUserByLogin(String login);
}
