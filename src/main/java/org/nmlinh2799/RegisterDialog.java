package org.nmlinh2799;

import controllers.RegisterController;
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

public class RegisterDialog {
    private static String fullname;
    private static String email;
    private static String username;
    private static String password;
    private static String confirmPassword;

    public static Optional<String> show() throws IOException {
        // Create dialog
        Dialog<String> dialog = new Dialog<>();
        ButtonType registerButtonType = new ButtonType("Đăng ký", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CLOSE);

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("register.fxml"));

        // Validate register
        Button registerButton = (Button) dialog.getDialogPane().lookupButton(registerButtonType);
        registerButton.addEventFilter(ActionEvent.ACTION, event -> {
            // Get register properties
            RegisterController registerController = fxmlLoader.getController();
            fullname = registerController.getFullname();
            email = registerController.getEmail();
            username = registerController.getUsername();
            password = registerController.getPassword();
            confirmPassword = registerController.getConfirmPassword();

            if (isEmptyAnyField()) {
                App.showAlert(Alert.AlertType.ERROR, "Thiếu thông tin",
                        "Vui lòng điền đầy đủ các trường thông tin");
                event.consume();
                return;
            }

            if (!confirmPassword.equals(password)) {
                App.showAlert(Alert.AlertType.ERROR, "Xác nhận mật khẩu không khớp",
                        "Vui lòng xác nhận lại mật khẩu");
                event.consume();
                return;
            }

            EntityManager entityManager = App.createEntityManager();
            boolean success;
            if (registerController.isUser()) {
                success = userRegister(entityManager);
            } else {
                success = adminRegister(entityManager);
            }
            entityManager.close();

            if (!success) {
                event.consume();
            }
        });

        // Set dialog content
        Parent parent = fxmlLoader.load();
        dialog.getDialogPane().setContent(parent);

        // Get result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                return "success";
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static boolean isEmptyAnyField() {
        return fullname.equals("")
                || email.equals("")
                || username.equals("")
                || password.equals("")
                || confirmPassword.equals("");
    }

    private static boolean userRegister(EntityManager entityManager) {
        UserDao userDao = new UserDao(entityManager);

        User findUsername = userDao.findByUsername(username);
        if (findUsername != null) {
            App.showAlert(Alert.AlertType.WARNING, "Tên tài khoản đã tồn tại",
                    "Vui lòng nhập tên tài khoản khác");
            return false;
        }

        User findEmail = userDao.findByEmail(email);
        if (findEmail != null) {
            App.showAlert(Alert.AlertType.WARNING, "Email đã tồn tại", "Vui lòng nhập email khác");
            return false;
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        userDao.persist(fullname, username, hash, email);
        return true;
    }

    private static boolean adminRegister(EntityManager entityManager) {
        AdminDao adminDao = new AdminDao(entityManager);

        Admin findUsername = adminDao.findByUsername(username);
        if (findUsername != null) {
            App.showAlert(Alert.AlertType.WARNING, "Tên tài khoản đã tồn tại",
                    "Vui lòng nhập tên tài khoản khác");
            return false;
        }

        Admin findEmail = adminDao.findByEmail(email);
        if (findEmail != null) {
            App.showAlert(Alert.AlertType.WARNING, "Email đã tồn tại", "Vui lòng nhập email khác");
            return false;
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
        adminDao.persist(fullname, username, hash, email);
        return true;
    }
}
