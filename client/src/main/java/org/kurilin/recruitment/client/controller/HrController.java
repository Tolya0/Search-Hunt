package org.kurilin.recruitment.client.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.kurilin.recruitment.client.Client;
import org.kurilin.recruitment.client.enums.StagePath;
import org.kurilin.recruitment.client.network.ClientSocket;
import org.kurilin.recruitment.client.utils.AlertUtil;
import org.kurilin.recruitment.client.utils.Loader;
import org.kurilin.recruitment.client.utils.ValidationUtil;
import org.kurilin.recruitment.shared.enums.*;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class HrController {
    @FXML
    private TabPane mainTabPane;
    // Vacancies Table
    @FXML
    private TableView<VacancyResponseDTO> vacanciesTable;
    @FXML
    private TableColumn<VacancyResponseDTO, Long> idColumn;
    @FXML
    private TableColumn<VacancyResponseDTO, String> titleColumn;
    @FXML
    private TableColumn<VacancyResponseDTO, String> deptColumn;
    @FXML
    private TableColumn<VacancyResponseDTO, String> salaryColumn;
    @FXML
    private TableColumn<VacancyResponseDTO, VacancyStatus> statusColumn;
    // Vacancy Form
    @FXML
    private TextField titleField;
    @FXML
    private TextField requirementsField;
    @FXML
    private TextField descriptionField;
    @FXML
    private TextField salaryMinField;
    @FXML
    private TextField salaryMaxField;
    @FXML
    private ComboBox<WorkFormat> workFormatComboBox;
    @FXML
    private ComboBox<DepartmentResponseDTO> deptComboBox;
    @FXML
    private Button submitButton;
    @FXML
    private Button closeButton;
    // Applications Table
    @FXML
    private TableView<ApplicationResponseDTO> applicationsTable;
    @FXML
    private TableColumn<ApplicationResponseDTO, Long> appIdColumn;
    @FXML
    private TableColumn<ApplicationResponseDTO, String> appNameColumn;
    @FXML
    private TableColumn<ApplicationResponseDTO, String> appSourceColumn;
    @FXML
    private TableColumn<ApplicationResponseDTO, LocalDateTime> appDateColumn;
    @FXML
    private TableColumn<ApplicationResponseDTO, ApplicationStatus> appStatusColumn;
    @FXML
    private Button viewProfileButton;
    @FXML
    private Button scoreButton;
    @FXML
    private Button inviteButton;
    @FXML
    private Button rejectButton;
    @FXML
    private Button offerButton;
    // Interviews
    @FXML
    private TableView<InterviewResponseDTO> interviewsTable;
    @FXML
    private TableColumn<InterviewResponseDTO, Long> intIdColumn;
    @FXML
    private TableColumn<InterviewResponseDTO, String> intCandidateColumn;
    @FXML
    private TableColumn<InterviewResponseDTO, String> intVacancyColumn;
    @FXML
    private TableColumn<InterviewResponseDTO, LocalDateTime> intDateColumn;
    @FXML
    private TableColumn<InterviewResponseDTO, String> intLocationColumn;
    @FXML
    private TableColumn<InterviewResponseDTO, InterviewStatus> intStatusColumn;
    @FXML
    private Label evalCandidateNameLabel;
    @FXML
    private TextField evalScoreField;
    @FXML
    private TextArea evalCommentsArea;
    @FXML
    private ComboBox<String> evalPassedCombo;
    @FXML
    private Button evalSubmitBtn;

    @FXML
    private TextField searchSkillsField;
    @FXML
    private TextField searchExpField;
    @FXML
    private TableView<CandidateResponseDTO> candidatesTable;
    @FXML
    private TableColumn<CandidateResponseDTO, Long> candIdCol;
    @FXML
    private TableColumn<CandidateResponseDTO, String> candNameCol;
    @FXML
    private TableColumn<CandidateResponseDTO, String> candEmailCol;
    @FXML
    private TableColumn<CandidateResponseDTO, Integer> candExpCol;
    @FXML
    private TableColumn<CandidateResponseDTO, String> candSkillsCol;
    @FXML
    private TableColumn<CandidateResponseDTO, Integer> candSalaryCol;


    @FXML
    private TextArea generalReportArea;
    @FXML
    private TextArea funnelReportArea;
    @FXML
    private ComboBox<VacancyResponseDTO> reportVacancyCombo;

    private final Gson gson = GsonFactory.getGson();
    private VacancyResponseDTO editingVacancy;
    private ApplicationResponseDTO selectedApplication;
    private InterviewResponseDTO selectedInterview;

    @FXML
    private void initialize() {
        vacanciesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        deptColumn.setCellValueFactory(new PropertyValueFactory<>("departmentName"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        salaryColumn.setCellValueFactory(cellData -> {
            VacancyResponseDTO vacancy = cellData.getValue();
            String salaryText = vacancy.getSalaryMin() + " - " + vacancy.getSalaryMax() + " USD";
            return new SimpleStringProperty(salaryText);
        });
        workFormatComboBox.getItems().addAll(WorkFormat.values());

        appIdColumn.setCellValueFactory(new PropertyValueFactory<>("Id"));
        appNameColumn.setCellValueFactory(new PropertyValueFactory<>("candidateName"));
        appSourceColumn.setCellValueFactory(new PropertyValueFactory<>("sourceName"));
        appDateColumn.setCellValueFactory(new PropertyValueFactory<>("appliedAt"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        appDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        appStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        intIdColumn.setCellValueFactory(new PropertyValueFactory<>("interviewId"));
        intCandidateColumn.setCellValueFactory(new PropertyValueFactory<>("candidateName"));
        intVacancyColumn.setCellValueFactory(new PropertyValueFactory<>("vacancyTitle"));
        intLocationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        intStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        intDateColumn.setCellValueFactory(new PropertyValueFactory<>("scheduledTime"));
        intDateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(formatter));
                }
            }
        });
        evalPassedCombo.getItems().addAll("Passed", "Failed");

        interviewsTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldSelection, newSelection) -> {
                    selectedInterview = newSelection;
                    if (newSelection != null) {
                        evalCandidateNameLabel.setText(newSelection.getCandidateName());
                        if (newSelection.getStatus() == InterviewStatus.COMPLETED) {
                            loadEvaluationForInterview(newSelection.getInterviewId());
                        } else {
                            evalScoreField.clear();
                            evalCommentsArea.clear();
                            evalPassedCombo.getSelectionModel().clearSelection();

                            evalScoreField.setDisable(false);
                            evalCommentsArea.setDisable(false);
                            evalPassedCombo.setDisable(false);
                            evalSubmitBtn.setDisable(false);
                        }
                    } else {
                        evalCandidateNameLabel.setText("Select an interview...");
                        evalSubmitBtn.setDisable(true);
                    }
                });

        salaryMinField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        salaryMaxField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));
        evalScoreField.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        reportVacancyCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(VacancyResponseDTO vacancy) {
                return vacancy != null ? vacancy.getTitle() : "";
            }

            @Override
            public VacancyResponseDTO fromString(String string) {
                return null;
            }
        });
        deptComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(DepartmentResponseDTO dept) {
                return dept != null ? dept.getName() : "";
            }

            @Override
            public DepartmentResponseDTO fromString(String string) {
                return null;
            }
        });

        loadDepartments();
        loadVacancies();
        loadInterviews();

        vacanciesTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        editingVacancy = newSelection;
                        submitButton.setText("Update Vacancy");
                        closeButton.setDisable(false);
                        deptComboBox.setDisable(true);

                        titleField.setText(editingVacancy.getTitle());
                        requirementsField.setText(editingVacancy.getRequirements());
                        descriptionField.setText(editingVacancy.getDescription());
                        salaryMinField.setText(String.valueOf(editingVacancy.getSalaryMin()));
                        salaryMaxField.setText(String.valueOf(editingVacancy.getSalaryMax()));
                        workFormatComboBox.getSelectionModel().select(editingVacancy.getWorkFormat());
                        loadApplicationsForVacancy(newSelection.getId());
                    } else {
                        handleClearVacancyForm();
                        applicationsTable.setItems(FXCollections.observableArrayList());
                    }
                });
        applicationsTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldSelection, newSelection) -> {
                    selectedApplication = newSelection;
                    boolean hasSelection = newSelection != null;
                    viewProfileButton.setDisable(!hasSelection);
                    scoreButton.setDisable(!hasSelection);
                    inviteButton.setDisable(!hasSelection);
                    rejectButton.setDisable(!hasSelection);
                    offerButton.setDisable(!hasSelection);
                });

        mainTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldSelection, newSelection) -> {
            if (newSelection.getText().equals("Manage Vacancies")) {
                loadVacancies();
            } else if (newSelection.getText().equals("Interviews")) {
                loadInterviews();
            }
        });

        candIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        candNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        candEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        candExpCol.setCellValueFactory(new PropertyValueFactory<>("experience"));
        candSkillsCol.setCellValueFactory(new PropertyValueFactory<>("skills"));
        candSalaryCol.setCellValueFactory(new PropertyValueFactory<>("expectedSalary"));

        searchExpField.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("\\d*") ? c : null));
    }

    private void loadEvaluationForInterview(Long interviewId) {
        EvaluationRequestDTO dto = new EvaluationRequestDTO(interviewId);
        Request request = new Request(RequestType.GET_EVALUATION, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            EvaluationResponseDTO eval = gson.fromJson(response.getPayload(), EvaluationResponseDTO.class);
            evalScoreField.setText(String.valueOf(eval.getScore()));
            evalCommentsArea.setText(eval.getComments() != null ? eval.getComments() : "");
            evalPassedCombo.getSelectionModel().select(eval.getIsPassed() ? "Passed" : "Failed");

            evalScoreField.setDisable(true);
            evalCommentsArea.setDisable(true);
            evalPassedCombo.setDisable(true);
            evalSubmitBtn.setDisable(true);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    private void loadInterviews() {
        HrInterviewsRequestDTO dto = HrInterviewsRequestDTO.builder()
                .hrManagerId(Client.getCurrentUser().getId())
                .build();
        Request request = new Request(RequestType.GET_HR_INTERVIEWS, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<InterviewResponseDTO>>() {
            }.getType();
            List<InterviewResponseDTO> interviews = gson.fromJson(response.getPayload(), listType);
            ObservableList<InterviewResponseDTO> observableList = FXCollections.observableArrayList(interviews);
            interviewsTable.setItems(observableList);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleSubmitEvaluation() {
        if (selectedInterview == null) {
            return;
        }
        String scoreStr = evalScoreField.getText().strip();
        if (ValidationUtil.isNullOrBlank(scoreStr) || evalPassedCombo.getValue() == null) {
            AlertUtil.error("Validation Error", "Score and passed status cannot be empty.");
            return;
        }
        int score = Integer.parseInt(scoreStr);
        if (score < 0 || score > 10) {
            AlertUtil.error("Validation Error", "Evaluation must be between 0 and 10.");
            return;
        }

        boolean isPassed = evalPassedCombo.getValue().contains("Passed");
        String comments = evalCommentsArea.getText().strip();

        EvaluationCreateRequestDTO dto = EvaluationCreateRequestDTO.builder()
                .interviewId(selectedInterview.getInterviewId())
                .evaluatorId(Client.getCurrentUser().getId())
                .score(score)
                .isPassed(isPassed)
                .comments(comments)
                .build();
        Request request = new Request(RequestType.ADD_EVALUATION, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", "Evaluation submitted successfully!");
            evalScoreField.clear();
            evalCommentsArea.clear();
            evalPassedCombo.getSelectionModel().clearSelection();
            loadInterviews();
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleLogout() {
        Client.setCurrentUser(null);
        Loader.loadScene(StagePath.LOGIN);
    }

    private void loadDepartments() {
        Request request = new Request(RequestType.GET_ALL_DEPARTMENTS, "{}");
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<DepartmentResponseDTO>>() {
            }.getType();
            List<DepartmentResponseDTO> departments = gson.fromJson(response.getPayload(), listType);
            deptComboBox.getItems().addAll(departments);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    private void loadVacancies() {
        VacancySearchRequestDTO dto = VacancySearchRequestDTO.builder()
                .keyword("")
                .wantedSalary(0)
                .build();
        Request request = new Request(RequestType.SEARCH_VACANCIES, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<VacancyResponseDTO>>() {
            }.getType();
            List<VacancyResponseDTO> vacancies = gson.fromJson(response.getPayload(), listType);
            ObservableList<VacancyResponseDTO> observableList = FXCollections.observableArrayList(vacancies);
            vacanciesTable.setItems(observableList);
            reportVacancyCombo.setItems(observableList);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleSubmitVacancy() {
        if (editingVacancy == null) {
            createNewVacancy();
        } else {
            updateExistingVacancy();
        }
    }

    private boolean isValidVacancyInput(boolean isCreate) {
        String title = titleField.getText();
        String reqs = requirementsField.getText();
        String desc = descriptionField.getText();
        String minSal = salaryMinField.getText();
        String maxSal = salaryMaxField.getText();
        WorkFormat format = workFormatComboBox.getValue();
        DepartmentResponseDTO dept = deptComboBox.getValue();

        if (!ValidationUtil.isValidVacancyText(title) ||
                !ValidationUtil.isValidVacancyText(reqs) ||
                !ValidationUtil.isValidVacancyText(desc)) {
            AlertUtil.error("Validation Error", "Vacancy title, requirements and description must be at least 3 characters long.");
            return false;
        }
        if (!ValidationUtil.isValidSalary(minSal, maxSal)) {
            AlertUtil.error("Validation Error", "Salary must be a number and min salary must be less than max salary.");
            return false;
        }
        if (!ValidationUtil.isComboSelected(format)) {
            AlertUtil.error("Validation Error", "Please select a work format.");
            return false;
        }
        if (isCreate && !ValidationUtil.isComboSelected(dept)) {
            AlertUtil.error("Validation Error", "Please select a department.");
            return false;
        }
        return true;
    }

    private void createNewVacancy() {
        if (!isValidVacancyInput(true)) return;

        VacancyCreateRequestDTO dto = VacancyCreateRequestDTO.builder()
                .title(titleField.getText())
                .requirements(requirementsField.getText())
                .description(descriptionField.getText())
                .salaryMin(Integer.parseInt(salaryMinField.getText()))
                .salaryMax(Integer.parseInt(salaryMaxField.getText()))
                .workFormat(workFormatComboBox.getValue())
                .departmentId(deptComboBox.getValue().getId())
                .hrManagerId(Client.getCurrentUser().getId())
                .build();
        sendRequestAndRefresh(RequestType.CREATE_VACANCY, gson.toJson(dto));
    }

    private void updateExistingVacancy() {
        if (!isValidVacancyInput(false)) return;

        VacancyUpdateRequestDTO dto = VacancyUpdateRequestDTO.builder()
                .id(editingVacancy.getId())
                .title(titleField.getText())
                .requirements(requirementsField.getText())
                .description(descriptionField.getText())
                .salaryMin(Integer.parseInt(salaryMinField.getText()))
                .salaryMax(Integer.parseInt(salaryMaxField.getText()))
                .workFormat(workFormatComboBox.getValue())
                .build();
        sendRequestAndRefresh(RequestType.UPDATE_VACANCY, gson.toJson(dto));
    }

    @FXML
    private void handleCloseVacancy() {
        if (editingVacancy != null) {
            VacancyCloseRequestDTO dto = VacancyCloseRequestDTO.builder()
                    .vacancyId(editingVacancy.getId())
                    .build();
            sendRequestAndRefresh(RequestType.CLOSE_VACANCY, gson.toJson(dto));
        }
    }

    @FXML
    private void handleClearVacancyForm() {
        editingVacancy = null;
        vacanciesTable.getSelectionModel().clearSelection();
        titleField.clear();
        requirementsField.clear();
        descriptionField.clear();
        salaryMinField.clear();
        salaryMaxField.clear();
        workFormatComboBox.getSelectionModel().clearSelection();
        deptComboBox.getSelectionModel().clearSelection();
        submitButton.setText("Create Vacancy");
        closeButton.setDisable(true);
        deptComboBox.setDisable(false);
    }

    private void sendRequestAndRefresh(RequestType requestType, String payload) {
        Request request = new Request(requestType, payload);
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", response.getMessage());
            handleClearVacancyForm();
            loadVacancies();
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    private void loadApplicationsForVacancy(Long vacancyId) {
        VacancyApplicationsRequestDTO dto = VacancyApplicationsRequestDTO.builder()
                .vacancyId(vacancyId)
                .build();
        Request request = new Request(RequestType.GET_VACANCY_APPLICATIONS, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<ApplicationResponseDTO>>() {
            }.getType();
            List<ApplicationResponseDTO> applications = gson.fromJson(response.getPayload(), listType);
            ObservableList<ApplicationResponseDTO> observableList = FXCollections.observableArrayList(applications);
            applicationsTable.setItems(observableList);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleCalculateScore() {
        if (selectedApplication == null) {
            return;
        }
        ScoringRequestDTO dto = ScoringRequestDTO.builder()
                .applicationId(selectedApplication.getId())
                .build();
        Request request = new Request(RequestType.CALCULATE_SCORING, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            ScoringResponseDTO scoring = gson.fromJson(response.getPayload(), ScoringResponseDTO.class);
            String title = "Match Result: " + scoring.getMatchPercentage() + "%";
            AlertUtil.info(title, scoring.getMatchDetails());
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleInviteToInterview() {
        if (selectedApplication == null) {
            return;
        }

        if (selectedApplication.getStatus() == ApplicationStatus.REJECTED ||
                selectedApplication.getStatus() == ApplicationStatus.HIRED ||
                selectedApplication.getStatus() == ApplicationStatus.INTERVIEW_SCHEDULED) {
            AlertUtil.error("Warning", "Cannot schedule interview for rejected or hired applications!");
            return;
        }
        Dialog<InterviewScheduleRequestDTO> dialog = new Dialog<>();
        dialog.setTitle("Schedule Interview");
        dialog.setHeaderText("Schedule interview for " + selectedApplication.getCandidateName());

        ButtonType scheduleButtonType = new ButtonType("Schedule", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(scheduleButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        DatePicker datePicker = new DatePicker();
        TextField timePicker = new TextField();
        timePicker.setPromptText("HH:mm (eg 14:30)");
        TextField locationPicker = new TextField();
        locationPicker.setPromptText("Zoom link or Office Room");

        grid.add(new Label("Date:"), 0, 0);
        grid.add(datePicker, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(timePicker, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationPicker, 1, 2);

        dialog.getDialogPane().setContent(grid);

        Button scheduleBtn = (Button) dialog.getDialogPane().lookupButton(scheduleButtonType);
        scheduleBtn.addEventFilter(ActionEvent.ACTION, event -> {
            String timeStr = timePicker.getText().strip();
            String location = locationPicker.getText().strip();

            if (datePicker.getValue() == null || timeStr.isBlank() || location.isBlank()) {
                AlertUtil.error("Validation Error", "All fields (Date, Time, Location) are required!");
                event.consume();
                return;
            }

            try {
                int hour = Integer.parseInt(timeStr.split(":")[0]);
                int minute = Integer.parseInt(timeStr.split(":")[1]);
            } catch (Exception e) {
                AlertUtil.error("Validation Error", "Time must be in HH:mm format (e.g., 14:30)!");
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == scheduleButtonType) {
                String timeStr = timePicker.getText().strip();
                int hour = Integer.parseInt(timeStr.split(":")[0]);
                int minute = Integer.parseInt(timeStr.split(":")[1]);
                LocalDateTime plannedTime = datePicker.getValue().atTime(hour, minute);
                String location = locationPicker.getText().strip();
                return InterviewScheduleRequestDTO.builder()
                        .hrManagerId(Client.getCurrentUser().getId())
                        .applicationId(selectedApplication.getId())
                        .plannedTime(plannedTime)
                        .location(location)
                        .build();
            }
            return null;
        });

        Optional<InterviewScheduleRequestDTO> result = dialog.showAndWait();

        result.ifPresent(dto -> {
            Request request = new Request(RequestType.SCHEDULE_INTERVIEW, gson.toJson(dto));
            String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
            Response response = gson.fromJson(jsonResponse, Response.class);
            if (response.isSuccess()) {
                AlertUtil.info("Success", "Interview scheduled successfully!");
                if (editingVacancy != null) {
                    loadApplicationsForVacancy(editingVacancy.getId());
                }
                loadInterviews();
            } else {
                AlertUtil.error("Error", response.getMessage());
            }
        });
    }

    @FXML
    private void handleRejectApplication() {
        if (selectedApplication != null && (selectedApplication.getStatus() == ApplicationStatus.REJECTED || selectedApplication.getStatus() == ApplicationStatus.HIRED)) {
            AlertUtil.warning("Warning", "Application is already rejected!");
            return;
        }
        updateApplicationStatus(ApplicationStatus.REJECTED);
    }

    private void updateApplicationStatus(ApplicationStatus newStatus) {
        if (selectedApplication == null) {
            return;
        }
        ApplicationStatusUpdateRequestDTO dto = ApplicationStatusUpdateRequestDTO.builder()
                .applicationId(selectedApplication.getId())
                .newStatus(newStatus)
                .build();
        Request request = new Request(RequestType.UPDATE_APPLICATION_STATUS, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", response.getMessage());
            if (editingVacancy != null) {
                loadApplicationsForVacancy(editingVacancy.getId());
            }
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    public void handleMakeOffer() {
        if (selectedApplication == null) {
            return;
        }
        if (selectedApplication.getStatus() == ApplicationStatus.REJECTED || selectedApplication.getStatus() == ApplicationStatus.HIRED) {
            AlertUtil.error("Warning", "Application is already hired or rejected!");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Make an Offer");
        dialog.setHeaderText("Enter offer amount for " + selectedApplication.getCandidateName());
        dialog.setContentText("Enter final salary (USD):");

        TextField editor = dialog.getEditor();
        editor.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d*")) {
                return change;
            }
            return null;
        }));

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (editor.getText().isBlank()) {
                AlertUtil.error("Validation Error", "Offer amount cannot be empty.");
                event.consume();
            }
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String offerAmountStr = result.get().strip();
            int offerAmount = Integer.parseInt(offerAmountStr);
            OfferRequestDTO dto = OfferRequestDTO.builder()
                    .applicationId(selectedApplication.getId())
                    .finalSalary(offerAmount)
                    .build();
            Request request = new Request(RequestType.GENERATE_OFFER, gson.toJson(dto));
            String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
            Response response = gson.fromJson(jsonResponse, Response.class);
            if (response.isSuccess()) {
                AlertUtil.info("Success", "Offer successfully generated and sent to the candidate!");
                if (editingVacancy != null) {
                    loadApplicationsForVacancy(editingVacancy.getId());
                }
            } else {
                AlertUtil.error("Error", response.getMessage());
            }
        }
    }

    @FXML
    private void handleViewProfile() {
        if (selectedApplication == null) {
            return;
        }
        String profileDetails = String.format(
                "Email: %s\nPhone: %s\n\nExperience: %d years\nSkills: %s\nResume URL: %s",
                selectedApplication.getCandidateEmail(),
                selectedApplication.getCandidatePhone(),
                selectedApplication.getCandidateExperience(),
                selectedApplication.getCandidateSkills() != null ? selectedApplication.getCandidateSkills() : "Not specified",
                selectedApplication.getCandidateResumeUrl() != null ? selectedApplication.getCandidateResumeUrl() : "Not provided"
        );

        AlertUtil.info("Candidate Profile: " + selectedApplication.getCandidateName(), profileDetails);
    }

    @FXML
    private void handleSearchCandidates() {
        String skills = searchSkillsField.getText().strip();
        String expStr = searchExpField.getText().strip();
        if (ValidationUtil.isNullOrBlank(skills) && ValidationUtil.isNullOrBlank(expStr)) {
            AlertUtil.error("Validation Error", "Please enter at least one search criteria (skills or experience).");
            return;
        }
        int experience = ValidationUtil.isNullOrBlank(expStr) ? 0 : Integer.parseInt(expStr);

        CandidateSearchRequestDTO dto = CandidateSearchRequestDTO.builder()
                .skills(skills)
                .minExperience(experience)
                .build();
        Request request = new Request(RequestType.SEARCH_CANDIDATES, gson.toJson(dto));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            Type listType = new TypeToken<List<CandidateResponseDTO>>() {
            }.getType();
            List<CandidateResponseDTO> candidates = gson.fromJson(response.getPayload(), listType);
            ObservableList<CandidateResponseDTO> observableList = FXCollections.observableArrayList(candidates);
            candidatesTable.setItems(observableList);
            if (candidates.isEmpty()) {
                AlertUtil.info("No Candidates Found", "No candidates found matching the specified criteria.");
            }
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleGenerateGeneralReports() {
        StringBuilder report = new StringBuilder();

        Request requestTime = new Request(RequestType.GENERATE_REPORT_TIME, "{}");
        String jsonResponseTime = ClientSocket.getInstance().sendMessage(gson.toJson(requestTime));
        Response responseTime = gson.fromJson(jsonResponseTime, Response.class);
        if (responseTime.isSuccess()) {
            TimeReportResponseDTO dto = gson.fromJson(responseTime.getPayload(), TimeReportResponseDTO.class);
            report.append("=== AVERAGE TIME TO FILL ===\n");
            report.append(dto.getAverageTimeToFillDays()).append(" days\n\n");
        } else {
            AlertUtil.error("Error", responseTime.getMessage());
            return;
        }

        Request requestSources = new Request(RequestType.GENERATE_REPORT_SOURCES, "{}");
        String jsonResponseSources = ClientSocket.getInstance().sendMessage(gson.toJson(requestSources));
        Response responseSources = gson.fromJson(jsonResponseSources, Response.class);
        if (responseSources.isSuccess()) {
            SourcesReportResponseDTO dto = gson.fromJson(responseSources.getPayload(), SourcesReportResponseDTO.class);
            report.append("=== CANDIDATE SOURCES ===\n");
            if (dto.getSourceStats() == null || dto.getSourceStats().isEmpty()) {
                report.append("No data available.\n");
            } else {
                dto.getSourceStats().forEach((source, count) ->
                        report.append("- ").append(source).append(": ").append(count).append(" applications\n")
                );
            }
        } else {
            AlertUtil.error("Error", responseSources.getMessage());
            return;
        }
        generalReportArea.setText(report.toString());
    }

    @FXML
    private void handleGenerateFunnel() {
        VacancyResponseDTO selected = reportVacancyCombo.getValue();
        if (selected == null) {
            AlertUtil.error("Error", "Please select a vacancy from the list first!");
            return;
        }

        FunnelReportRequestDTO dtoReq = new FunnelReportRequestDTO(selected.getId());
        Request request = new Request(RequestType.GENERATE_REPORT_FUNNEL, gson.toJson(dtoReq));
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);

        if (response.isSuccess()) {
            FunnelReportResponseDTO dtoResp = gson.fromJson(response.getPayload(), FunnelReportResponseDTO.class);
            StringBuilder report = new StringBuilder("=== HIRING FUNNEL ===\n");
            report.append("Vacancy: ").append(selected.getTitle()).append("\n\n");

            if (dtoResp.getFunnelData() == null || dtoResp.getFunnelData().isEmpty()) {
                report.append("No applications for this vacancy yet.");
            } else {
                dtoResp.getFunnelData().forEach((status, count) ->
                        report.append(status.name()).append(": ").append(count).append(" candidates\n")
                );
            }
            funnelReportArea.setText(report.toString());
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleSaveGeneralReport() {
        String content = generalReportArea.getText();
        if (ValidationUtil.isNullOrBlank(content)) {
            AlertUtil.warning("Warning", "Please generate a report first before saving!");
            return;
        }
        saveTextToFile("General_HR_Report.txt", content);
    }

    @FXML
    private void handleSaveFunnelReport() {
        String content = funnelReportArea.getText();
        if (ValidationUtil.isNullOrBlank(content)) {
            AlertUtil.warning("Warning", "Please build a funnel first!");
            return;
        }

        String vacancyName = reportVacancyCombo.getValue() != null ? reportVacancyCombo.getValue().getTitle().replaceAll(" ", "_") : "Vacancy";
        saveTextToFile("Funnel_Report_" + vacancyName + ".txt", content);
    }

    private void saveTextToFile(String defaultFileName, String content) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Report");
        fileChooser.setInitialFileName(defaultFileName);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));

        File file = fileChooser.showSaveDialog(Client.getPrimaryStage());

        if (file != null) {
            try {
                Files.writeString(file.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                AlertUtil.info("Success", "Report successfully saved!\nPath: " + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.error("Error", "Failed to save the file: " + e.getMessage());
            }
        }
    }
}
