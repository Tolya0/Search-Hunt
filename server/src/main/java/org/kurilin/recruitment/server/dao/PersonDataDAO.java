package org.kurilin.recruitment.server.dao;

import org.kurilin.recruitment.shared.entity.PersonData;

import java.util.Optional;

public interface PersonDataDAO extends GenericDAO<PersonData> {
    Optional<PersonData> findByEmail(String email);
    Optional<PersonData> findByPhone(String phone);
}
