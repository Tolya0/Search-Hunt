package org.kurilin.recruitment.client.controller;

import com.google.gson.Gson;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.kurilin.recruitment.client.enums.StagePath;
import org.kurilin.recruitment.client.network.ClientSocket;
import org.kurilin.recruitment.client.utils.AlertUtil;
import org.kurilin.recruitment.client.utils.Loader;
import org.kurilin.recruitment.client.utils.ValidationUtil;
import org.kurilin.recruitment.shared.enums.RequestType;
import org.kurilin.recruitment.shared.enums.SexType;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.CandidateRegistrationRequestDTO;
import org.kurilin.recruitment.shared.util.GsonFactory;

import java.time.LocalDate;

public class RegisterCandidateController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private ComboBox<SexType> sexComboBox;
    @FXML
    private TextField experienceField;
    @FXML
    private TextField skillsField;
    @FXML
    private TextField salaryField;
    @FXML
    private TextField resumeField;

    private final Gson gson = GsonFactory.getGson();

    @FXML
    private void initialize() {
        sexComboBox.getItems()
                .addAll(SexType.values());
        experienceField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
        salaryField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));
    }

    @FXML
    private void handleRegister() {
        String login = usernameField.getText();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();
        String fullName = fullNameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        LocalDate birthDate = birthDatePicker.getValue();
        SexType sex = sexComboBox.getValue();
        String experienceStr = experienceField.getText();
        String skills = skillsField.getText();
        String salaryStr = salaryField.getText();

        if (!ValidationUtil.isValidLogin(login) || !ValidationUtil.isValidPassword(pass) ||
                !ValidationUtil.isValidFullName(fullName) || !ValidationUtil.isValidEmail(email) ||
                !ValidationUtil.isValidPhone(phone) || ValidationUtil.isNullOrBlank(skills)) {
            AlertUtil.error("Validation Error", "Please fill all fields correctly.");
            return;
        }
        if (!ValidationUtil.isValidPasswordConfirmation(pass, confirmPass)) {
            AlertUtil.error("Validation Error", "Passwords do not match.");
            return;
        }
        if (birthDate == null || sex == null) {
            AlertUtil.error("Validation Error", "Please select Date of Birth and Sex.");
            return;
        }
        int experienceInt = 0;
        int expectedSalaryInt = 0;
        try {
            experienceInt = Integer.parseInt(experienceStr);
            expectedSalaryInt = Integer.parseInt(salaryStr);
            if (experienceInt < 0 || expectedSalaryInt < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            AlertUtil.error("Validation Error", "Experience must be a number.");
            return;
        }

        CandidateRegistrationRequestDTO dto = CandidateRegistrationRequestDTO.builder()
                .username(login)
                .password(pass)
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .birthDate(birthDate)
                .sex(sex)
                .experience(experienceInt)
                .skills(skills)
                .expectedSalary(expectedSalaryInt)
                .resumeURL(resumeField.getText())
                .build();

        Request request = new Request(RequestType.REGISTER_CANDIDATE, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", response.getMessage());
            Loader.loadScene(StagePath.LOGIN);
        } else {
            AlertUtil.error("Registration Failed", response.getMessage());
        }
    }

    @FXML
    private void handleBackToLogin() {
        Loader.loadScene(StagePath.LOGIN);
    }
}
