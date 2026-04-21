package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.UserDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.User;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

public class UserDAOImpl extends GenericDAOImpl<User> implements UserDAO {
    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);

    @Override
    public Optional<User> findUserByLogin(String login) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from User u where u.username = :login";
            Optional<User> user = session.createQuery(query, User.class)
                    .setParameter("login", login)
                    .uniqueResultOptional();
            logger.info("User found: {}", user.isPresent());
            return user;
        } catch (Exception e) {
            logger.error("Error finding user: {}", login, e);
            throw new DaoException("Error finding user: " + login, e);
        }
    }
}
