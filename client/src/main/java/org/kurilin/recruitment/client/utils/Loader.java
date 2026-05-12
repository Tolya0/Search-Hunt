package org.kurilin.recruitment.client.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import org.kurilin.recruitment.client.Client;
import org.kurilin.recruitment.client.enums.StagePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class Loader {
    private static final Logger logger = LoggerFactory.getLogger(Loader.class);
    public static void loadScene(StagePath stagePath) {
        try {
            FXMLLoader loader = new FXMLLoader(Client.class.getResource(stagePath.getPath()));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            Client.getPrimaryStage().setScene(scene);
            Client.getPrimaryStage().sizeToScene();
            Client.getPrimaryStage().centerOnScreen();
        } catch (IOException e) {
            logger.error("Error loading scene: {}", stagePath.name(), e);
            AlertUtil.error("Navigation error", "Failed to load screen: " + stagePath.name());
        }
    }
}
