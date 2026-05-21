package org.kurilin.recruitment.client.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.kurilin.recruitment.client.Client;
import org.kurilin.recruitment.client.enums.StagePath;
import org.kurilin.recruitment.client.network.ClientSocket;
import org.kurilin.recruitment.client.utils.AlertUtil;
import org.kurilin.recruitment.client.utils.Loader;
import org.kurilin.recruitment.client.utils.ValidationUtil;
import org.kurilin.recruitment.shared.enums.ApplicationStatus;
import org.kurilin.recruitment.shared.enums.RequestType;
import org.kurilin.recruitment.shared.enums.WorkFormat;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CandidateController {
    @FXML
    private TabPane mainTabPane;
    ;
    @FXML
    private TextField searchKeywordField;
    @FXML
    private TextField searchSalaryField;
    @FXML
    private TableView<VacancyResponseDTO> searchTable;
    @FXML
    private TableColumn<VacancyResponseDTO, String> searchTitleCol;
    @FXML
    private TableColumn<VacancyResponseDTO, String> searchDeptCol;
    @FXML
    private TableColumn<VacancyResponseDTO, String> searchSalaryCol;
    @FXML
    private TableColumn<VacancyResponseDTO, WorkFormat> searchFormatCol;
    @FXML
    private Label detailsReqLabel;
    @FXML
    private Label detailsDescLabel;
    @FXML
    private Button applyButton;

    @FXML
    private TableView<MyApplicationResponseDTO> myAppsTable;
    @FXML
    private TableColumn<MyApplicationResponseDTO, String> myAppTitleCol;
    @FXML
    private TableColumn<MyApplicationResponseDTO, String> myAppDeptCol;
    @FXML
    private TableColumn<MyApplicationResponseDTO, ApplicationStatus> myAppStatusCol;
    @FXML
    private TableColumn<MyApplicationResponseDTO, LocalDateTime> myAppDateCol;
    @FXML
    private TableColumn<MyApplicationResponseDTO, String> myAppOfferCol;
    @FXML
    private Button acceptOfferBtn;
    @FXML
    private Button declineOfferBtn;

    @FXML
    private TextField profileFullNameField;
    @FXML
    private TextField profileEmailField;
    @FXML
    private TextField profilePhoneField;
    @FXML
    private DatePicker profileBirthDatePicker;
    @FXML
    private TextField profileExperienceField;
    @FXML
    private TextField profileSkillsField;
    @FXML
    private TextField profileSalaryField;
    @FXML
    private TextField profileResumeField;
    @FXML
    private PasswordField oldPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private ComboBox<SourceResponseDTO> sourceComboBox;

    private final Gson gson = GsonFactory.getGson();
    private VacancyResponseDTO selectedVacancy;
    private Long currentCandidateId;
    private MyApplicationResponseDTO selectedApplication;
    private final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @FXML
    private void initialize() {
        searchTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        searchDeptCol.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        searchSalaryCol.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSalaryMin() + " - " + cell.getValue().getSalaryMax() + " USD"));
        searchFormatCol.setCellValueFactory(new PropertyValueFactory<>("workFormat"));

        searchTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldSelection, newSelection) -> {
                    selectedVacancy = newSelection;
                    if (newSelection != null) {
                        detailsReqLabel.setText(newSelection.getRequirements());
                        detailsDescLabel.setText(newSelection.getDescription());
                        applyButton.setDisable(false);
                    } else {
                        detailsReqLabel.setText("Select a vacancy...");
                        detailsDescLabel.setText("Select a vacancy...");
                        applyButton.setDisable(true);
                    }
                });

        myAppsTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldSelection, newSelection) -> {
                    selectedApplication = newSelection;
                    if (newSelection != null) {
                        if (newSelection.getStatus() == ApplicationStatus.OFFERED) {
                            acceptOfferBtn.setDisable(false);
                            declineOfferBtn.setDisable(false);
                        } else {
                            acceptOfferBtn.setDisable(true);
                            declineOfferBtn.setDisable(true);
                        }
                    } else {
                        acceptOfferBtn.setDisable(true);
                        declineOfferBtn.setDisable(true);
                    }
                });

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection.getText().equals("My Applications")) {
                loadMyApplications();
            } else if (newSelection.getText().equals("Search Vacancies")) {
                handleSearchVacancies();
            }
        });


        myAppTitleCol.setCellValueFactory(new PropertyValueFactory<>("vacancyTitle"));
        myAppDeptCol.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        myAppStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        myAppOfferCol.setCellValueFactory(new PropertyValueFactory<>("offerText"));
        myAppDateCol.setCellValueFactory(new PropertyValueFactory<>("appliedAt"));
        myAppDateCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMATTER));
                }
            }
        });
        searchSalaryField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        profileExperienceField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        profileSalaryField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        populateProfileFields();
        handleSearchVacancies();
        loadMyApplications();
        loadSources();
    }

    @FXML
    private void handleSearchVacancies() {
        int salary = 0;
        String keyword = searchKeywordField.getText().strip();
        if (!ValidationUtil.isNullOrBlank(searchSalaryField.getText())) {
            salary = Integer.parseInt(searchSalaryField.getText().strip());
        }
        VacancySearchRequestDTO dto = VacancySearchRequestDTO.builder()
                .keyword(keyword)
                .wantedSalary(salary)
                .build();
        Request request = new Request(RequestType.SEARCH_VACANCIES, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<VacancyResponseDTO>>() {
            }.getType();
            List<VacancyResponseDTO> vacancies = gson.fromJson(response.getPayload(), listType);
            searchTable.setItems(FXCollections.observableArrayList(vacancies));
        }
    }

    @FXML
    private void handleApply() {
        if (selectedVacancy == null) {
            return;
        }
        ApplicationCreateRequestDTO dto = ApplicationCreateRequestDTO.builder()
                .candidateId(currentCandidateId)
                .vacancyId(selectedVacancy.getId())
                .sourceId(sourceComboBox.getValue() != null ? sourceComboBox.getValue().getId() : null)
                .build();
        Request request = new Request(RequestType.APPLY_FOR_VACANCY, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", "Your application has been submitted successfully.");
            loadMyApplications();
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    private void loadSources() {
        Request request = new Request(RequestType.GET_ALL_SOURCES, "{}");
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<SourceResponseDTO>>() {
            }.getType();
            List<SourceResponseDTO> sources = gson.fromJson(response.getPayload(), listType);
            sourceComboBox.getItems().addAll(sources);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    private void loadMyApplications() {
        MyApplicationsRequestDTO dto = MyApplicationsRequestDTO.builder()
                .candidateId(currentCandidateId)
                .build();
        Request request = new Request(RequestType.GET_MY_APPLICATIONS, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<MyApplicationResponseDTO>>() {
            }.getType();
            List<MyApplicationResponseDTO> applications = gson.fromJson(response.getPayload(), listType);
            myAppsTable.setItems(FXCollections.observableArrayList(applications));
        }
    }

    private void populateProfileFields() {
        CandidateProfileRequestDTO dto = CandidateProfileRequestDTO.builder()
                .id(Client.getCurrentUser().getId())
                .build();
        Request request = new Request(RequestType.GET_CANDIDATE_PROFILE, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            CandidateResponseDTO profile = gson.fromJson(response.getPayload(), CandidateResponseDTO.class);
            currentCandidateId = profile.getId();
            profileFullNameField.setText(profile.getFullName());
            profileEmailField.setText(profile.getEmail());
            profilePhoneField.setText(profile.getPhone());
            profileBirthDatePicker.setValue(profile.getBirthDate());
            profileExperienceField.setText(profile.getExperience() != null ? String.valueOf(profile.getExperience()) : "0");
            profileSkillsField.setText(profile.getSkills() != null ? profile.getSkills() : "");
            profileSalaryField.setText(profile.getExpectedSalary() != null ? String.valueOf(profile.getExpectedSalary()) : "0");
            profileResumeField.setText(profile.getResumeURL() != null ? profile.getResumeURL() : "");
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleUpdateProfile() {
        int experience = ValidationUtil.isNullOrBlank(profileExperienceField.getText()) ? 0 : Integer.parseInt(profileExperienceField.getText());
        int salary = ValidationUtil.isNullOrBlank(profileSalaryField.getText()) ? 0 : Integer.parseInt(profileSalaryField.getText());
        LocalDate date = profileBirthDatePicker.getValue();
        if (date != null && !ValidationUtil.isValidDate(date)) {
            AlertUtil.error("Validation Error", "Invalid Date of Birth. Cannot be in the future or under 14 years old.");
            return;
        }
        CandidateUpdateRequestDTO dto = CandidateUpdateRequestDTO.builder()
                .id(currentCandidateId)
                .fullName(profileFullNameField.getText().strip())
                .email(profileEmailField.getText().strip())
                .phone(profilePhoneField.getText().strip())
                .birthDate(profileBirthDatePicker.getValue())
                .experience(experience)
                .skills(profileSkillsField.getText().strip())
                .expectedSalary(salary)
                .resumeURL(profileResumeField.getText().strip())
                .build();
        Request request = new Request(RequestType.UPDATE_PROFILE, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", "Profile updated successfully.");
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleChangePassword() {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        if (!ValidationUtil.isValidPassword(oldPassword) || !ValidationUtil.isValidPassword(newPassword)) {
            AlertUtil.error("Error", "Passwords must be at least 6 characters long.");
            return;
        }
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .userId(Client.getCurrentUser().getId())
                .oldPassword(oldPassword)
                .newPassword(newPassword)
                .build();
        Request request = new Request(RequestType.CHANGE_PASSWORD, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", "Password changed successfully.");
            oldPasswordField.clear();
            newPasswordField.clear();
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Client.setCurrentUser(null);
        Loader.loadScene(StagePath.LOGIN);
    }

    @FXML
    private void handleAcceptOffer() {
        updateApplicationStatus(ApplicationStatus.HIRED);
    }

    @FXML
    private void handleDeclineOffer() {
        updateApplicationStatus(ApplicationStatus.REJECTED);
    }

    private void updateApplicationStatus(ApplicationStatus status) {
        if (selectedApplication == null) {
            return;
        }
        ApplicationStatusUpdateRequestDTO dto = ApplicationStatusUpdateRequestDTO.builder()
                .applicationId(selectedApplication.getApplicationId())
                .newStatus(status)
                .build();
        Request request = new Request(RequestType.UPDATE_APPLICATION_STATUS, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", "Your response has been sent to HR!");
            loadMyApplications();
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }
}

