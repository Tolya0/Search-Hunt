package org.kurilin.recruitment.server.dao.impl;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.kurilin.recruitment.server.dao.GenericDAO;
import org.kurilin.recruitment.server.utils.HibernateSessionFactory;
import org.kurilin.recruitment.shared.exception.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

public class GenericDAOImpl<T> implements GenericDAO<T> {
    private static final Logger logger = LoggerFactory.getLogger(GenericDAOImpl.class);

    private void rollbackTransaction(Transaction transaction) {
        if (transaction != null) {
            transaction.rollback();
            logger.error("Transaction rolled back");
        }
    }

    @Override
    public void save(T entity){
        Transaction transaction = null;
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(entity);
            transaction.commit();
            logger.info("Entity saved: {}", entity.getClass().getSimpleName());
        } catch (Exception e) {
            rollbackTransaction(transaction);
            logger.error("Error saving entity: {}", entity.getClass().getSimpleName(), e);
            throw new DaoException("Error saving entity: " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void update(T entity){
        Transaction transaction = null;
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(entity);
            transaction.commit();
            logger.info("Entity updated: {}", entity.getClass().getSimpleName());
        } catch (Exception e) {
            rollbackTransaction(transaction);
            logger.error("Error updating entity: {}", entity.getClass().getSimpleName(), e);
            throw new DaoException("Error updating entity: " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public void delete(T entity) {
        Transaction transaction = null;
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(entity);
            transaction.commit();
            logger.info("Entity deleted: {}", entity.getClass().getSimpleName());
        } catch (Exception e) {
            rollbackTransaction(transaction);
            logger.error("Error deleting entity: {}", entity.getClass().getSimpleName(), e);
            throw new DaoException("Error deleting entity: " + entity.getClass().getSimpleName(), e);
        }
    }

    @Override
    public Optional<T> findById(Class<T> clazz, Long id) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            T entity = session.find(clazz, id);
            if (entity != null) {
                logger.info("Entity was found: {} with id: {}", clazz.getSimpleName(), id);
            } else {
                logger.warn("Entity not found: {} with id: {}", clazz.getSimpleName(), id);
            }
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            logger.error("Error finding entity: {}", clazz.getSimpleName(), e);
            throw new DaoException("Error finding entity: " + clazz.getSimpleName(), e);
        }
    }

    @Override
    public List<T> findAll(Class<T> clazz) {
        try (Session session = HibernateSessionFactory.getSessionFactory().openSession()) {
            List<T> entities = session.createQuery("from " + clazz.getSimpleName(), clazz).getResultList();
            logger.info("Entities found: {} count: {}", clazz.getSimpleName(), entities.size());
            return entities;
        } catch (Exception e) {
            logger.error("Error finding all entities: {}", clazz.getSimpleName(), e);
            throw new DaoException("Error finding all entities: " + clazz.getSimpleName(), e);
        }
    }
}
