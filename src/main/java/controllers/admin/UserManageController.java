package controllers.admin;

import daos.UserDao;
import entities.User;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class UserManageController implements Initializable {
    @FXML
    private TextField keywordsField;
    @FXML
    private ChoiceBox<String> searchChoice;
    @FXML
    private ChoiceBox<String> statusChoice;
    @FXML
    private TableView<User> userTableView;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> fullnameColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, Integer> noConfAttendedColumn; // Number of conferences attended
    @FXML
    private TableColumn<User, Boolean> blockedColumn;

    private ObservableList<User> userObservableList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Row details
        userTableView.setRowFactory(tv -> new TableRow<>() {
            Node detailsPane;

            {
                addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
                    final int index = getIndex();
                    if (index >= 0 && index < userTableView.getItems().size() && userTableView.getSelectionModel().isSelected(index)) {
                        userTableView.getSelectionModel().clearSelection();
                        event.consume();
                    }
                });
                selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                    if (isNowSelected) {
                        getChildren().add(detailsPane);
                    } else {
                        getChildren().remove(detailsPane);
                    }
                    this.requestLayout();
                });
                try {
                    detailsPane = createDetailsPane(itemProperty());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected double computePrefHeight(double width) {
                if (isSelected()) {
                    return super.computePrefHeight(width) + detailsPane.prefHeight(getWidth());
                } else {
                    return super.computePrefHeight(width);
                }
            }

            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                if (isSelected()) {
                    double width = getWidth();
                    double paneHeight = detailsPane.prefHeight(width);
                    detailsPane.resizeRelocate(0, getHeight() - paneHeight, width, paneHeight);
                }
            }
        });

        //==============================================================================================
        // Set choice boxes
        searchChoice.getItems().addAll("Họ tên", "Tên tài khoản", "Email");
        searchChoice.setValue("Họ tên");
        statusChoice.getItems().addAll("Tất cả trạng thái", "Bị chặn", "Không bị chặn");
        statusChoice.setValue("Tất cả trạng thái");

        // Set list
        EntityManager entityManager = App.createEntityManager();
        userObservableList = FXCollections.observableList((new UserDao(entityManager)).selectAll());
        entityManager.close();

        // Set columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        fullnameColumn.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        noConfAttendedColumn.setCellValueFactory(new PropertyValueFactory<>("noConfAttended"));
        blockedColumn.setCellValueFactory(new PropertyValueFactory<>("blocked"));
        userTableView.setItems(userObservableList);
    }

    public void search() {
        EntityManager entityManager = App.createEntityManager();
        userObservableList = FXCollections.observableList(
                (new UserDao(entityManager)).search(
                        keywordsField.getText(), searchChoice.getValue(), statusChoice.getValue()));
        entityManager.close();
        userTableView.setItems(userObservableList);
    }

    public void block() {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            App.showAlert(Alert.AlertType.ERROR, "Bạn chưa chọn Người dùng nào",
                    "Vui lòng chọn một Người dùng trong danh sách để thực hiện tác vụ");
            return;
        }
        EntityManager entityManager = App.createEntityManager();
        UserDao userDao = new UserDao(entityManager);
        userDao.block(selected.getId());
        userObservableList = FXCollections.observableList(userDao.selectAll());
        entityManager.close();
        userTableView.setItems(userObservableList);
    }

    public void unblock() {
        User selected = userTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            App.showAlert(Alert.AlertType.ERROR, "Bạn chưa chọn Người dùng nào",
                    "Vui lòng chọn một Người dùng trong danh sách để thực hiện tác vụ");
            return;
        }
        EntityManager entityManager = App.createEntityManager();
        UserDao userDao = new UserDao(entityManager);
        userDao.unblock(selected.getId());
        userObservableList = FXCollections.observableList(userDao.selectAll());
        entityManager.close();
        userTableView.setItems(userObservableList);
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("primary"));
        stage.setScene(scene);
    }

    private Node createDetailsPane(ObjectProperty<User> item) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("conferencesListPane.fxml"));
        Node detailsPane = fxmlLoader.load();

        ConferencesListPaneController controller = fxmlLoader.getController();
        item.addListener((obs, oldItem, newItem) -> controller.setListView(newItem));

        return detailsPane;
    }
}
