package org.kurilin.recruitment.client.controller;

import org.kurilin.recruitment.client.Client;
import org.kurilin.recruitment.client.enums.StagePath;
import org.kurilin.recruitment.client.utils.AlertUtil;
import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.kurilin.recruitment.client.network.ClientSocket;
import org.kurilin.recruitment.client.utils.Loader;
import org.kurilin.recruitment.shared.enums.RequestType;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.LoginRequestDTO;
import org.kurilin.recruitment.shared.network.dto.UserResponseDTO;
import org.kurilin.recruitment.shared.util.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    private final Gson gson = GsonFactory.getGson();

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            logger.error("Username and password are required");
            AlertUtil.error("Validation Error", "Username and password are required");
            return;
        }

        LoginRequestDTO dto = LoginRequestDTO.builder()
                .username(username)
                .password(password)
                .build();

        Request request = new Request(RequestType.LOGIN, gson.toJson(dto));

        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));

        Response response = gson.fromJson(jsonResponse, Response.class);

        if (response.isSuccess()) {
            logger.info("Login successful");
            UserResponseDTO user = gson.fromJson(response.getPayload(), UserResponseDTO.class);
            Client.setCurrentUser(user);
            AlertUtil.info("Success", response.getMessage() + " Welcome, " + user.getUsername() + "!");

            switch (user.getRole()) {
                case ADMIN -> Loader.loadScene(StagePath.DASHBOARD_ADMIN);
                case CANDIDATE -> Loader.loadScene(StagePath.DASHBOARD_CANDIDATE);
                case HR_MANAGER -> Loader.loadScene(StagePath.DASHBOARD_HR);
            }
        } else {
            logger.error("Login failed: {}", response.getMessage());
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleGoToRegister() {
        Loader.loadScene(StagePath.REGISTER_CANDIDATE);
    }
}
