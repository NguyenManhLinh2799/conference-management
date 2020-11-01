package controllers;

import controllers.admin.LocationManageController;
import daos.ConferenceDao;
import entities.Conference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.nmlinh2799.App;
import org.nmlinh2799.LoginDialog;
import org.nmlinh2799.RegisterDialog;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PrimaryController implements Initializable {
    @FXML
    private MenuButton accountMenuBtn;
    @FXML
    private MenuItem loginMenuItem;
    @FXML
    private MenuItem registerMenuItem;
    @FXML
    private MenuItem profileMenuItem;
    @FXML
    private MenuItem logoutMenuItem;

    // Sidebar properties
    @FXML
    private Label roleLabel;
    @FXML
    private Button conferenceStatBtn;
    @FXML
    private Button conferenceManageBtn;
    @FXML
    private Button userManageBtn;
    @FXML
    private Button locationBtn;

    // Table properties
    @FXML
    private TableView<Conference> conferenceTableView;
    @FXML
    private FlowPane conferenceFlowPane;
    @FXML
    private TableColumn<Conference, Integer> idColumn;
    @FXML
    private TableColumn<Conference, String> nameColumn;
    @FXML
    private TableColumn<Conference, String> summaryColumn;

    // A class of each items in cardview display
    class ConferenceVBox extends VBox {
        public ConferenceVBox(Conference conference) throws URISyntaxException {
            ImageView imageView = new ImageView();
            Image image = new Image(
                    App.class.getResource("/images/" + conference.getImage()).toURI().toString(),
                    120, 100, false, true);
            imageView.setImage(image);

            Label name = new Label();
            name.setPrefWidth(120);
            name.setWrapText(true);
            name.setText(conference.getId() + ". " + conference.getName());
            this.getChildren().addAll(imageView, name);
            setMargin(name, new Insets(10, 10, 10, 10));
            setMargin(imageView, new Insets(10, 10, 0, 10));

            this.setOnMouseClicked(e -> {
                try {
                    showConferenceInfo(e, conference);
                } catch (IOException | URISyntaxException ioException) {
                    ioException.printStackTrace();
                }
            });
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // When user login
        if (App.getUser() != null) {
            roleLabel.setText("Phân hệ: Người dùng");
            roleLabel.setVisible(true);
            conferenceStatBtn.setDisable(false);

            // Menu
            accountMenuBtn.setText(App.getUser().getFullname());
            loginMenuItem.setVisible(false);
            registerMenuItem.setVisible(false);
            profileMenuItem.setVisible(true);
            logoutMenuItem.setVisible(true);
        } else if (App.getAdmin() != null) // When admin login
        {
            roleLabel.setText("Phân hệ: Quản trị viên");
            roleLabel.setVisible(true);
            conferenceManageBtn.setDisable(false);
            userManageBtn.setDisable(false);
            locationBtn.setDisable(false);
            userManageBtn.setDisable(false);

            // Menu
            accountMenuBtn.setText(App.getAdmin().getFullname());
            loginMenuItem.setVisible(false);
            registerMenuItem.setVisible(false);
            profileMenuItem.setVisible(true);
            logoutMenuItem.setVisible(true);
        }

        // Select all conferences
        EntityManager entityManager = App.createEntityManager();
        ConferenceDao conferenceDao = new ConferenceDao(entityManager);
        ObservableList<Conference> conferenceObservableList = FXCollections.observableList(conferenceDao.selectAll());
        entityManager.close();

        // List display
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        summaryColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        conferenceTableView.setItems(conferenceObservableList);

        // Cardview display
        for (Conference conference : conferenceObservableList) {
            ConferenceVBox conferenceVBox = null;
            try {
                conferenceVBox = new ConferenceVBox(conference);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            conferenceFlowPane.getChildren().add(conferenceVBox);
        }
    }

    public void onRowSelected(MouseEvent e) throws IOException, URISyntaxException {
        Conference selected = conferenceTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showConferenceInfo(e, selected);
        }
    }

    public void showConferenceInfo(MouseEvent e, Conference conference) throws IOException, URISyntaxException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("conferenceInfo.fxml"));
        Parent conferenceInfoParent = fxmlLoader.load();
        Scene scene = new Scene(conferenceInfoParent);

        // Set conference info
        ConferenceInfoController conferenceInfoController = fxmlLoader.getController();
        conferenceInfoController.setConference(conference);
        conferenceInfoController.setPrevious("primary");

        stage.setScene(scene);
    }

    public void login() throws IOException {
        Optional<String> result = LoginDialog.show();
        result.ifPresent(action -> {
            Stage stage = (Stage) accountMenuBtn.getScene().getWindow();
            Scene scene = null;
            try {
                scene = new Scene(App.loadFXML("primary"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.setScene(scene);
            App.showAlert(Alert.AlertType.INFORMATION, "Đăng nhập thành công", "");
        });
    }

    public void logout() throws IOException {
        App.setUser(null);
        App.setAdmin(null);
        Stage stage = (Stage) accountMenuBtn.getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("primary"));
        stage.setScene(scene);
        App.showAlert(Alert.AlertType.INFORMATION, "Bạn đã đăng xuất", "");
    }

    public void registerAccount() throws IOException {
        Optional<String> result = RegisterDialog.show();
        result.ifPresent(action -> {
            Stage stage = (Stage) accountMenuBtn.getScene().getWindow();
            Scene scene = null;
            try {
                scene = new Scene(App.loadFXML("primary"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            stage.setScene(scene);
            App.showAlert(Alert.AlertType.INFORMATION, "Tạo tài khoản thành công", "");
        });
    }

    public void showProfile() throws IOException {
        Stage stage = (Stage) accountMenuBtn.getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("profile"));
        stage.setScene(scene);
    }

    public void showConferenceStat(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("conferenceStat"));
        stage.setScene(scene);
    }

    public void showConferenceManage(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("conferenceManage"));
        stage.setScene(scene);
    }

    public void showUserManage(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("userManage"));
        stage.setScene(scene);
    }

    public void showLocationManage(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("locationManage.fxml"));
        Parent locationManageParent = fxmlLoader.load();
        Scene scene = new Scene(locationManageParent);

        // Set conference info
        LocationManageController locationManageController = fxmlLoader.getController();
        locationManageController.setPrevious("primary");

        stage.setScene(scene);
    }
}
