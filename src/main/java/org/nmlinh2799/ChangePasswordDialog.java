package org.nmlinh2799;

import controllers.ChangePasswordController;
import daos.AdminDao;
import daos.UserDao;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.util.Optional;

public class ChangePasswordDialog {
    private static String oldPassword;
    private static String newPassword;
    private static String confirm;

    public static Optional<String> show() throws IOException {
        // Create dialog
        Dialog<String> dialog = new Dialog<>();
        ButtonType changePasswordBtnType = new ButtonType("Đổi mật khẩu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changePasswordBtnType, ButtonType.CLOSE);

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("changePassword.fxml"));

        // Validate old and new password
        Button changePasswordBtn = (Button) dialog.getDialogPane().lookupButton(changePasswordBtnType);
        changePasswordBtn.addEventFilter(ActionEvent.ACTION, event -> {
            ChangePasswordController controller = fxmlLoader.getController();
            oldPassword = controller.getOldPass();
            newPassword = controller.getNewPass();
            confirm = controller.getConfirm();

            if (oldPassword.equals("") || newPassword.equals("") || confirm.equals("")) {
                App.showAlert(Alert.AlertType.ERROR, "Vui lòng điền đầy đủ các trường thông tin", "");
                event.consume();
                return;
            }

            // Check if old password is incorrect
            int id = 0;
            if (App.getUser() != null) {
                id = App.getUser().getId();
            } else if (App.getAdmin() != null) {
                id = App.getAdmin().getId();
            }
            EntityManager entityManager = App.createEntityManager();
            String oldHashed = "";
            if (App.getUser() != null) {
                oldHashed = (new UserDao(entityManager)).find(id).getPassword();
            } else if (App.getAdmin() != null) {
                oldHashed = (new AdminDao(entityManager)).find(id).getPassword();
            }

            if (!BCrypt.checkpw(oldPassword, oldHashed)) {
                App.showAlert(Alert.AlertType.WARNING, "Mật khẩu cũ không đúng",
                        "Vui lòng nhập lại mật khẩu cũ");
                entityManager.close();
                event.consume();
                return;
            }

            // Check confirm password
            if (!confirm.equals(newPassword)) {
                App.showAlert(Alert.AlertType.ERROR, "Xác nhận mật khẩu không khớp",
                        "Vui lòng xác nhận lại mật khẩu");
                entityManager.close();
                event.consume();
                return;
            }

            String hash = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
            if (App.getUser() != null) {
                (new UserDao(entityManager)).changePassword(id, hash);
            } else if (App.getAdmin() != null) {
                (new AdminDao(entityManager)).changePassword(id, hash);
            }
            entityManager.close();
        });

        // Set dialog content
        Parent parent = fxmlLoader.load();
        dialog.getDialogPane().setContent(parent);

        // Get result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changePasswordBtnType) {
                return "success";
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
