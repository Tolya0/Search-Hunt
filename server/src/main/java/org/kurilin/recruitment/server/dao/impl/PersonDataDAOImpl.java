package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.kurilin.recruitment.server.dao.PersonDataDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.entity.PersonData;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PersonDataDAOImpl extends GenericDAOImpl<PersonData> implements PersonDataDAO {
    private static final Logger logger = LoggerFactory.getLogger(PersonDataDAOImpl.class);

    @Override
    public Optional<PersonData> findByEmail(String email) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from PersonData p where p.email = :email";
            Optional<PersonData> personData = session.createQuery(query, PersonData.class)
                    .setParameter("email", email)
                    .uniqueResultOptional();
            logger.info("Person found by email {}: {}", email, personData.isPresent());
            return personData;
        } catch (Exception e) {
            logger.error("Error finding person by email: {}", email, e);
            throw new DaoException("Error finding person by email: " + email, e);
        }
    }

    @Override
    public Optional<PersonData> findByPhone(String phone) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            String query = "from PersonData p where p.phone = :phone";
            Optional<PersonData> personData = session.createQuery(query, PersonData.class)
                    .setParameter("phone", phone)
                    .uniqueResultOptional();
            logger.info("Person found by phone {}: {}", phone, personData.isPresent());
            return personData;
        } catch (Exception e) {
            logger.error("Error finding person by phone: {}", phone, e);
            throw new DaoException("Error finding person by phone: " + phone, e);
        }
    }
}
