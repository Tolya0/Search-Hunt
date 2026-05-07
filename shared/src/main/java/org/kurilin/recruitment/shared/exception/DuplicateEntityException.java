package org.kurilin.recruitment.shared.exception;

public class DuplicateEntityException extends RecruitmentBusinessException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
