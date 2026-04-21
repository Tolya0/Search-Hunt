package org.kurilin.recruitment.shared.exception;

public class DaoException extends RuntimeException {
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}
