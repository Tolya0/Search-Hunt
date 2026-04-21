package org.kurilin.recruitment.server.utils;


import org.slf4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.LoggerFactory;


public class HibernateSessionFactory {
    private static final Logger logger = LoggerFactory.getLogger(HibernateSessionFactory.class);
    private static volatile SessionFactory sessionFactory;

    private HibernateSessionFactory() {
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            synchronized (HibernateSessionFactory.class) {
                if (sessionFactory == null) {
                    try {
                        Configuration configuration = new Configuration().configure();
                        sessionFactory = configuration.buildSessionFactory();
                        logger.info("Hibernate SessionFactory created.");
                    } catch (Throwable ex) {
                        logger.error("Initial SessionFactory creation failed.", ex);
                        throw new ExceptionInInitializerError("Initial SessionFactory creation failed: " + ex.getMessage());
                    }
                }
            }
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed.");
        }
    }
}
