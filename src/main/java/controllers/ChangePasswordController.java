package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;

public class ChangePasswordController {
    @FXML
    private PasswordField oldPassField;
    @FXML
    private PasswordField newPassField;
    @FXML
    private PasswordField confirmField;

    public String getOldPass() {
        return oldPassField.getText();
    }

    public String getNewPass() {
        return newPassField.getText();
    }

    public String getConfirm() {
        return confirmField.getText();
    }
}
