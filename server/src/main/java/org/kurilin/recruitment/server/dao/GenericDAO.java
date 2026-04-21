package org.kurilin.recruitment.server.dao;

import java.util.List;
import java.util.Optional;

public interface GenericDAO<T>{
    void save(T entity);
    void update(T entity);
    void delete(T entity);
    Optional<T> findById(Class<T> clazz, Long id);
    List<T> findAll(Class<T> clazz);
}
