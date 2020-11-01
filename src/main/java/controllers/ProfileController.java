package controllers;

import daos.AdminDao;
import daos.UserDao;
import entities.Admin;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.nmlinh2799.App;
import org.nmlinh2799.ChangePasswordDialog;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {
    @FXML
    private TextField fullnameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (App.getUser() != null)
        {
            fullnameField.setText(App.getUser().getFullname());
            usernameField.setText(App.getUser().getUsername());
            emailField.setText(App.getUser().getEmail());
        }
        else if (App.getAdmin() != null)
        {
            fullnameField.setText(App.getAdmin().getFullname());
            usernameField.setText(App.getAdmin().getUsername());
            emailField.setText(App.getAdmin().getEmail());
        }
    }

    public void save() {
        if (fullnameField.getText().equals("") || emailField.getText().equals("")) {
            App.showAlert(Alert.AlertType.ERROR, "Thiếu thông tin",
                    "Vui lòng điền đầy đủ các trường thông tin");
            return;
        }

        EntityManager entityManager = App.createEntityManager();
        boolean success = false;
        if (App.getUser() != null) {
            success = updateUser(entityManager);
        } else if (App.getAdmin() != null) {
            success = updateAdmin(entityManager);
        }
        entityManager.close();

        if (!success) {
            return;
        }

        App.showAlert(Alert.AlertType.INFORMATION, "Cập nhật thành công", "");
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("primary"));
        stage.setScene(scene);
    }

    public boolean updateUser(EntityManager entityManager) {
        UserDao userDao = new UserDao(entityManager);

        // Check if there's another person already used the email
        User findEmail = userDao.findByEmail(emailField.getText());
        if (findEmail != null && findEmail.getId() != App.getUser().getId()) {
            App.showAlert(Alert.AlertType.WARNING, "Email đã có người sử dụng", "Vui lòng nhập email khác");
            return false;
        }

        // Update user
        userDao.update(App.getUser().getId(), fullnameField.getText(), emailField.getText());
        App.setUser(userDao.find(App.getUser().getId()));
        return true;
    }

    public boolean updateAdmin(EntityManager entityManager) {
        AdminDao adminDao = new AdminDao(entityManager);

        // Check if there's another person already used the email
        Admin findEmail = adminDao.findByEmail(emailField.getText());
        if (findEmail != null && findEmail.getId() == App.getAdmin().getId()) {
            App.showAlert(Alert.AlertType.WARNING, "Email đã có người sử dụng", "Vui lòng nhập email khác");
            return false;
        }

        // Update admin
        adminDao.update(App.getAdmin().getId(), fullnameField.getText(), emailField.getText());
        App.setAdmin(adminDao.find(App.getAdmin().getId()));
        return true;
    }

    public void changePassword() throws IOException {
        Optional<String> result = ChangePasswordDialog.show();
        result.ifPresent(action -> App.showAlert(Alert.AlertType.INFORMATION, "Đổi mật khẩu thành công", ""));
    }
}
