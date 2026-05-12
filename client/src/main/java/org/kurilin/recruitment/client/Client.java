package org.kurilin.recruitment.client;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.kurilin.recruitment.client.enums.StagePath;
import org.kurilin.recruitment.client.network.ClientSocket;
import org.kurilin.recruitment.client.utils.Loader;
import org.kurilin.recruitment.shared.network.dto.UserResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class Client extends Application {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    @Getter
    @Setter
    private static UserResponseDTO currentUser;
    @Getter
    private static Stage primaryStage;
    private static boolean isDarkTheme = false;

    public static void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        if (isDarkTheme) {
            Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            logger.info("Switched to dark theme");
        } else {
            Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            logger.info("Switched to light theme");
        }
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        primaryStage.setTitle("SearchAndHunt");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(Client.class.getResourceAsStream("/logo.png"))));
        Loader.loadScene(StagePath.LOGIN);

        primaryStage.setOnCloseRequest(event -> {
            ClientSocket.getInstance().closeConnection();
        });

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
