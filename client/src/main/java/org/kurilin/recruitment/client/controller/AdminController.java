package org.kurilin.recruitment.client.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.kurilin.recruitment.client.Client;
import org.kurilin.recruitment.client.enums.StagePath;
import org.kurilin.recruitment.client.network.ClientSocket;
import org.kurilin.recruitment.client.utils.AlertUtil;
import org.kurilin.recruitment.client.utils.Loader;
import org.kurilin.recruitment.client.utils.ValidationUtil;
import org.kurilin.recruitment.shared.enums.RequestType;
import org.kurilin.recruitment.shared.enums.Role;
import org.kurilin.recruitment.shared.enums.SexType;
import org.kurilin.recruitment.shared.network.Request;
import org.kurilin.recruitment.shared.network.Response;
import org.kurilin.recruitment.shared.network.dto.*;
import org.kurilin.recruitment.shared.util.GsonFactory;

import java.lang.reflect.Type;
import java.util.List;

public class AdminController {
    @FXML
    private TableView<UserResponseDTO> usersTable;
    @FXML
    private TableColumn<UserResponseDTO, Long> idColumn;
    @FXML
    private TableColumn<UserResponseDTO, String> usernameColumn;
    @FXML
    private TableColumn<UserResponseDTO, String> fullNameColumn;
    @FXML
    private TableColumn<UserResponseDTO, String> roleColumn;
    @FXML
    private TextField regUsernameField;
    @FXML
    private PasswordField regPasswordField;
    @FXML
    private PasswordField regConfirmPasswordField;
    @FXML
    private TextField regFullNameField;
    @FXML
    private TextField regEmailField;
    @FXML
    private TextField regPhoneField;
    @FXML
    private ComboBox<SexType> regSexComboBox;
    @FXML
    private DatePicker regBirthDatePicker;
    @FXML
    private ComboBox<Role> regRoleComboBox;
    @FXML
    private Button blockButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button submitButton;
    @FXML
    private Button clearButton;

    private final Gson gson = GsonFactory.getGson();

    private UserResponseDTO editingUser;

    @FXML
    private void initialize() {
        usersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        regRoleComboBox.getItems().addAll(Role.ADMIN, Role.HR_MANAGER);
        regSexComboBox.getItems().addAll(SexType.values());

        usersTable.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable,
                              oldSelection,
                              newSelection) -> {
                    if (newSelection != null) {
                        editingUser = newSelection;

                        blockButton.setDisable(false);
                        deleteButton.setDisable(false);
                        if (newSelection.getIsBlock()) {
                            blockButton.setText("Unblock");
                        } else {
                            blockButton.setText("Block");
                        }
                        submitButton.setText("Update User");
                        regUsernameField.setText(newSelection.getUsername());
                        regUsernameField.setDisable(true);
                        regPasswordField.setDisable(true);
                        regConfirmPasswordField.setDisable(true);
                        regBirthDatePicker.setDisable(true);
                        regSexComboBox.setDisable(true);

                        regFullNameField.setText(newSelection.getFullName());
                        regEmailField.setText(newSelection.getEmail());
                        regPhoneField.setText(newSelection.getPhone());
                        regRoleComboBox.getSelectionModel().select(newSelection.getRole());

                    } else {
                        handleClearForm();
                    }
                });
        loadUsers();
    }

    @FXML
    private void handleLogout() {
        Client.setCurrentUser(null);
        Loader.loadScene(StagePath.LOGIN);
    }

    private void loadUsers() {
        Request request = new Request(RequestType.GET_ALL_USERS, "{}");
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);

        if (response.isSuccess()) {
            Type listType = new TypeToken<List<UserResponseDTO>>() {}.getType();
            List<UserResponseDTO> users = gson.fromJson(response.getPayload(), listType);
            ObservableList<UserResponseDTO> observableList = FXCollections.observableArrayList(users);
            usersTable.setItems(observableList);
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleSubmit() {
        if (editingUser == null) {
            createNewUser();
        } else {
            updateExistingUser();
        }
    }

    private void createNewUser() {
        String login = regUsernameField.getText();
        String password = regPasswordField.getText();
        String fullName = regFullNameField.getText();
        String email = regEmailField.getText();
        String phone = regPhoneField.getText();

        if (ValidationUtil.isNullOrBlank(login) ||
                ValidationUtil.isNullOrBlank(password) ||
                ValidationUtil.isNullOrBlank(fullName) ||
                ValidationUtil.isNullOrBlank(email) ||
                ValidationUtil.isNullOrBlank(phone)) {
            AlertUtil.error("Validation Error", "All fields are required");
            return;
        }

        if (!ValidationUtil.isValidLogin(login)) {
            AlertUtil.error("Validation Error", "Invalid login format. Only letters and numbers are allowed.");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            AlertUtil.error("Validation Error", "Password must be at least 6 characters long.");
            return;
        }
        if (!ValidationUtil.isValidPasswordConfirmation(password, regConfirmPasswordField.getText())) {
            AlertUtil.error("Validation Error", "Passwords do not match.");
            return;
        }

        if (!isValidUserInput(fullName, email, phone)) return;

        if (regRoleComboBox.getValue() == null || regSexComboBox.getValue() == null) {
            AlertUtil.error("Validation Error", "Please fill in all required fields.");
            return;
        }
        if (!ValidationUtil.isValidDate(regBirthDatePicker.getValue())) {
            AlertUtil.error("Validation Error", "Invalid Date of Birth. User must be at least 14 years old.");
            return;
        }

        UserCreateRequestDTO user = UserCreateRequestDTO.builder()
                .username(regUsernameField.getText())
                .password(regPasswordField.getText())
                .fullName(regFullNameField.getText())
                .email(regEmailField.getText())
                .phone(regPhoneField.getText())
                .birthDate(regBirthDatePicker.getValue())
                .role(regRoleComboBox.getValue())
                .sex(regSexComboBox.getValue())
                .build();

        sendRequestAndRefresh(RequestType.ADD_USER, gson.toJson(user));

    }

    private void updateExistingUser() {
        String fullName = regFullNameField.getText();
        String email = regEmailField.getText();
        String phone = regPhoneField.getText();

        if (ValidationUtil.isNullOrBlank(fullName) ||
                ValidationUtil.isNullOrBlank(email) ||
                ValidationUtil.isNullOrBlank(phone)) {
            AlertUtil.error("Validation Error", "Full name, email and phone are required");
            return;
        }

        if (!isValidUserInput(fullName, email, phone)) return;

        UserUpdateRequestDTO dto = UserUpdateRequestDTO.builder()
                .id(editingUser.getId())
                .fullName(fullName)
                .email(email)
                .phone(phone)
                .role(regRoleComboBox.getValue())
                .build();

        sendRequestAndRefresh(RequestType.UPDATE_USER, gson.toJson(dto));
    }

    private boolean isValidUserInput(String fullName, String email, String phone) {
        if (!ValidationUtil.isValidEmail(email)) {
            AlertUtil.error("Validation Error", "Invalid email format.");
            return false;
        }

        if (!ValidationUtil.isValidFullName(fullName)) {
            AlertUtil.error("Validation Error", "Full name must be at least 2 characters long.");
            return false;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            AlertUtil.error("Validation Error", "Invalid phone number format.");
            return false;
        }
        return true;
    }

    private void sendRequestAndRefresh(RequestType requestType, String payload) {
        Request request = new Request(requestType, payload);
        String jsonResponse = ClientSocket.getInstance().sendMessage(gson.toJson(request));
        Response response = gson.fromJson(jsonResponse, Response.class);
        if (response.isSuccess()) {
            AlertUtil.info("Success", response.getMessage());
            loadUsers();
        } else {
            AlertUtil.error("Error", response.getMessage());
        }
    }

    @FXML
    private void handleClearForm() {
        usersTable.getSelectionModel().clearSelection();
        editingUser = null;

        regUsernameField.setDisable(false);
        regPasswordField.setDisable(false);
        regConfirmPasswordField.setDisable(false);
        regBirthDatePicker.setDisable(false);
        regSexComboBox.setDisable(false);

        regUsernameField.clear();
        regPasswordField.clear();
        regConfirmPasswordField.clear();
        regFullNameField.clear();
        regEmailField.clear();
        regPhoneField.clear();
        regBirthDatePicker.setValue(null);
        regRoleComboBox.getSelectionModel().clearSelection();
        regSexComboBox.getSelectionModel().clearSelection();

        submitButton.setText("Create User");
        blockButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleBlockUser() {
        if (editingUser == null) {
            return;
        }

        if (editingUser.getRole() == Role.ADMIN) {
            AlertUtil.warning("Security", "Cannot block admin account!");
            return;
        }
        boolean isBlocked = !editingUser.getIsBlock();
        UserBlockRequestDTO dto = UserBlockRequestDTO.builder()
                .id(editingUser.getId())
                .isBlock(isBlocked)
                .build();

        sendRequestAndRefresh(RequestType.BLOCK_USER, gson.toJson(dto));

    }

    @FXML
    private void handleDeleteUser() {
        if (editingUser == null) {
            return;
        }

        if (editingUser.getRole() == Role.ADMIN) {
            AlertUtil.warning("Security", "Cannot delete admin account!");
            return;
        }

        if (AlertUtil.confirmation("Confirm Deletion", "Are you sure you want to delete user " + editingUser.getFullName() + "?") == ButtonType.OK) {
            UserDeleteRequestDTO dto = UserDeleteRequestDTO.builder()
                    .id(editingUser.getId())
                    .build();
            sendRequestAndRefresh(RequestType.DELETE_USER, gson.toJson(dto));

        }
    }
}
