package controllers;

import daos.RegConfDao;
import entities.Conference;
import entities.RegisterConference;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.nmlinh2799.App;
import org.nmlinh2799.LoginDialog;
import org.nmlinh2799.RegisterDialog;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;

public class ConferenceInfoController {
    private String previous;
    private Conference conference;

    @FXML
    private Label id;
    @FXML
    private Label name;
    @FXML
    private Label summary;
    @FXML
    private Label detail;
    @FXML
    private ImageView imageView;
    @FXML
    private Label time;
    @FXML
    private Label loc;
    @FXML
    private Label numberOfRegistrations;
    @FXML
    private Label numberOfAttendees;
    @FXML
    private Button registerOrCancelBtn;

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    public void setConference(Conference conference) throws URISyntaxException {
        this.conference = conference;

        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);

        // Set column
        id.setText(String.valueOf(conference.getId()));
        name.setText(conference.getName());
        summary.setText(conference.getSummary());
        detail.setText(conference.getDetail());
        time.setText(conference.getDateOnly());
        loc.setText(String.valueOf(conference.getLocation().toString()));
        numberOfRegistrations.setText(String.valueOf(regConfDao.countRegConf(conference.getId())));
        numberOfAttendees.setText(String.valueOf(conference.getNumberOfAttendees()));

        // Set image
        Image image = new Image(
                App.class.getResource("/images/" + conference.getImage()).toURI().toString(),
                200, 150, false, true);
        imageView.setImage(image);

        // Check if user already registered this conference
        if (App.getUser() != null) {
            RegisterConference find = regConfDao.find(App.getUser().getId(), this.conference.getId());

            if (find != null) // Already registered
            {
                registerOrCancelBtn.setText("Hủy đăng ký");
                registerOrCancelBtn.setOnAction(event -> cancelRegistration());
            } else {
                registerOrCancelBtn.setText("Đăng ký tham dự");
                registerOrCancelBtn.setOnAction(event -> registerConference());
            }
        }

        entityManager.close();
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML(previous));
        stage.setScene(scene);
    }

    public void registerConference() {
        // If not login
        if (App.getUser() == null && App.getAdmin() == null) {
            Optional<String> result = showDialog();
            result.ifPresent(option -> {
                if (option.equals("login")) {
                    try {
                        login();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                } else if (option.equals("register")) {
                    try {
                        registerAccount();
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            });
            return;
        }

        // Admin isn't allowed to register conference
        if (App.getAdmin() != null) {
            App.showAlert(Alert.AlertType.WARNING, "Quản trị viên không có quyền hạn đăng ký tham dự Hội nghị",
                    "Vui lòng đăng nhập với vai trò 'Người dùng bình thường' để thực hiện tác vụ này");
            return;
        }

        // Check if registration is expired
        if (this.conference.getTime().before(new Date())) {
            App.showAlert(Alert.AlertType.WARNING, "Đã hết hạn đăng ký vào ngày " + time.getText(), "");
            return;
        }

        // Full slot
        if (Integer.parseInt(numberOfRegistrations.getText()) >= Integer.parseInt(numberOfAttendees.getText())) {
            App.showAlert(Alert.AlertType.WARNING, "Hội nghị đã đủ số lượng đăng ký", "");
            return;
        }

        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);
        regConfDao.persist(App.getUser().getId(), this.conference.getId());
        entityManager.close();

        refresh();

        App.showAlert(Alert.AlertType.INFORMATION, "Đăng ký tham dự hoàn tất",
                "Yêu cầu đăng ký tham dự đã được gửi cho QTV xem xét");
    }

    public Optional<String> showDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setHeaderText("Bạn chưa đăng nhập");

        // Create Login and Register account buttons
        ButtonType loginButtonType = new ButtonType("Đăng nhập", ButtonBar.ButtonData.OK_DONE);
        ButtonType registerButtonType = new ButtonType("Đăng ký tài khoản", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, registerButtonType, ButtonType.CLOSE);

        StackPane stackPane = new StackPane();

        Label loginRequired = new Label();
        loginRequired.setPrefWidth(300);
        loginRequired.setText("Bạn phải đăng nhập với vai trò 'Người dùng bình thường' mới được phép đăng ký tham dự hội nghị, chọn 'Đăng nhập' nếu bạn đã có tài khoản, chọn 'Đăng ký tài khoản' nếu chưa có");
        loginRequired.setWrapText(true);

        stackPane.getChildren().add(loginRequired);

        dialog.getDialogPane().setContent(stackPane);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return "login";
            } else if (dialogButton == registerButtonType) {
                return "register";
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public void login() throws IOException {
        Optional<String> result = LoginDialog.show();
        result.ifPresent(foo -> {
            refresh();
            App.showAlert(Alert.AlertType.INFORMATION, "Đăng nhập thành công", "");
        });
    }

    public void registerAccount() throws IOException {
        Optional<String> result = RegisterDialog.show();
        result.ifPresent(foo -> App.showAlert(Alert.AlertType.INFORMATION, "Tạo tài khoản thành công", ""));
    }

    public void showAttendeesList() throws IOException {
        Dialog<Object> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("attendeesList.fxml"));
        Parent attendeesListParent = fxmlLoader.load();
        AttendeesListController attendeesListController = fxmlLoader.getController();
        attendeesListController.setAttendees(this.conference.getId());

        dialog.getDialogPane().setContent(attendeesListParent);
        dialog.show();
    }

    public void cancelRegistration() {
        // Cannot cancel one already occurred
        if (this.conference.getTime().before(new Date())) {
            App.showAlert(Alert.AlertType.WARNING,
                    "Không thể hủy tham dự Hội nghị đã diễn ra",
                    "");
            return;
        }

        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);
        regConfDao.cancelRegistration(App.getUser().getId(), this.conference.getId());
        entityManager.close();

        refresh();

        App.showAlert(Alert.AlertType.INFORMATION, "Đã hủy đăng ký tham dự Hội nghị", "");
    }

    private void refresh() {
        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);

        // Reset number of registrations
        numberOfRegistrations.setText(String.valueOf(regConfDao.countRegConf(this.conference.getId())));

        // Check if user already register this conference
        if (App.getUser() != null) {
            RegisterConference find = regConfDao.find(App.getUser().getId(), this.conference.getId());
            if (find != null) // Already registered
            {
                registerOrCancelBtn.setText("Hủy đăng ký");
                registerOrCancelBtn.setOnAction(event -> cancelRegistration());
            } else {
                registerOrCancelBtn.setText("Đăng ký tham dự");
                registerOrCancelBtn.setOnAction(event -> registerConference());
            }
        }

        entityManager.close();
    }
}
