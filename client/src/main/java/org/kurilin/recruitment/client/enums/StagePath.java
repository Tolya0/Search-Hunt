package org.kurilin.recruitment.client.enums;

import lombok.Getter;

@Getter
public enum StagePath {
    LOGIN("/Login.fxml"),
    DASHBOARD_ADMIN("/AdminDashboard.fxml"),
    DASHBOARD_HR("/HrDashboard.fxml"),
    DASHBOARD_CANDIDATE("/CandidateDashboard.fxml"),
    REGISTER_CANDIDATE("/RegisterCandidate.fxml");

    private final String path;

    StagePath(String path) {
        this.path = path;
    }
}
