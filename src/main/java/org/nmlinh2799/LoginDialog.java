package org.nmlinh2799;

import controllers.LoginController;
import daos.AdminDao;
import daos.UserDao;
import entities.Admin;
import entities.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Optional;

public class LoginDialog {
    private static String username;
    private static String password;

    public static Optional<String> show() throws IOException {
        Dialog<String> dialog = new Dialog<>();
        // Create dialog
        ButtonType loginButtonType = new ButtonType("Đăng nhập", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CLOSE);

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("login.fxml"));

        // Validate login
        Button loginButton = (Button) dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Get login properties
            LoginController loginController = fxmlLoader.getController();
            username = loginController.getUsername();
            password = loginController.getPassword();

            if (loginController.getUsername().equals("") || loginController.getPassword().equals("")) {
                App.showAlert(Alert.AlertType.ERROR, "Thiếu thông tin", "Vui lòng điền đầy đủ các trường thông tin");
                event.consume();
                return;
            }

            EntityManager entityManager = App.createEntityManager();
            boolean success;
            if (loginController.isUser()) {
                success = userLogin(entityManager);
            } else {
                success = adminLogin(entityManager);
            }
            entityManager.close();

            if (!success) { // Not success
                event.consume();
            }
        });

        // Set dialog content
        Parent parent = fxmlLoader.load();
        dialog.getDialogPane().setContent(parent);

        // Get result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType)
            {
                return "success";
            }
            return null;
        });

        return dialog.showAndWait();
    }

    private static boolean userLogin(EntityManager entityManager) {
        UserDao userDao = new UserDao(entityManager);

        // Find user by username
        User findResult = userDao.findByUsername(username);

        // Wrong username
        if (findResult == null) {
            App.showAlert(Alert.AlertType.WARNING, "Tài khoản không tồn tại", "Vui nhập lại tên tài khoản");
            return false;
        }

        // Wrong password
        if (!BCrypt.checkpw(password, findResult.getPassword())) {
            App.showAlert(Alert.AlertType.WARNING, "Sai mật khẩu", "Vui lòng nhập lại mật khẩu");
            return false;
        }

        // Blocked
        if (findResult.isBlocked()) {
            App.showAlert(Alert.AlertType.WARNING, "Tài khoản của bạn đã bị chặn truy cập",
                    "Liên hệ qua email nmlinh@gmail.com để biết thêm chi tiết");
            return false;
        }

        App.setUser(findResult);
        return true;
    }

    private static boolean adminLogin(EntityManager entityManager) {
        AdminDao adminDao = new AdminDao(entityManager);

        // Find admin by username
        Admin findResult = adminDao.findByUsername(username);

        // Wrong username
        if (findResult == null) {
            App.showAlert(Alert.AlertType.WARNING, "Tài khoản không tồn tại", "Vui nhập lại tên tài khoản");
            return false;
        }

        // Wrong password
        if (!BCrypt.checkpw(password, findResult.getPassword())) {
            App.showAlert(Alert.AlertType.WARNING, "Sai mật khẩu", "Vui lòng nhập lại mật khẩu");
            return false;
        }

        App.setAdmin(findResult);
        return true;
    }
}
