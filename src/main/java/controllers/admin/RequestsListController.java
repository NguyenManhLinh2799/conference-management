package controllers.admin;

import daos.ConferenceDao;
import daos.RegConfDao;
import entities.Conference;
import entities.RegisterConference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;

public class RequestsListController {
    private int idConference;

    @FXML
    private TableView<RegisterConference> requestsTableView;
    @FXML
    private TableColumn<RegisterConference, Integer> idCol;
    @FXML
    private TableColumn<RegisterConference, String> fullnameCol;
    @FXML
    private TableColumn<RegisterConference, String> emailCol;
    @FXML
    private TableColumn<RegisterConference, String> statusCol;
    @FXML
    private Label numberOfRegistrations;
    @FXML
    private Label numberOfAttendees;

    private ObservableList<RegisterConference> requestsObservableList;

    public void setRequests(int idConference) {
        this.idConference = idConference;

        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);
        requestsObservableList = FXCollections.observableList(regConfDao.selectByConference(idConference));


        idCol.setCellValueFactory(new PropertyValueFactory<>("idUser"));
        fullnameCol.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        requestsTableView.setItems(requestsObservableList);

        numberOfRegistrations.setText(String.valueOf(regConfDao.countRegConf(idConference)));
        ConferenceDao conferenceDao = new ConferenceDao(entityManager);
        Conference conference = conferenceDao.find(idConference);
        numberOfAttendees.setText(String.valueOf(conference.getNumberOfAttendees()));

        entityManager.close();
    }

    public void accept() {
        RegisterConference selected = requestsTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            App.showAlert(Alert.AlertType.ERROR, "Bạn chưa chọn yêu cầu nào",
                    "Vui lòng chọn một yêu cầu trong danh sách để thực hiện tác vụ");
            return;
        }

        if (!selected.getStatus().equals("Đang chờ")) {
            App.showAlert(Alert.AlertType.WARNING, "Chỉ có thể chấp nhận yêu cầu đang chờ", "");
            return;
        }

        // Accept request and reload list
        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);

        regConfDao.acceptReq(selected.getIdUser(), selected.getIdConference());
        requestsObservableList = FXCollections.observableList(regConfDao.selectByConference(this.idConference));
        numberOfRegistrations.setText(String.valueOf(regConfDao.countRegConf(this.idConference)));

        entityManager.close();

        requestsTableView.setItems(requestsObservableList);
        App.showAlert(Alert.AlertType.INFORMATION, "1 Yêu cầu tham dự đã được chấp nhận", "");
    }

    public void decline() {
        RegisterConference selected = requestsTableView.getSelectionModel().getSelectedItem();

        if (selected == null) {
            App.showAlert(Alert.AlertType.ERROR, "Bạn chưa chọn yêu cầu nào",
                    "Vui lòng chọn một yêu cầu trong danh sách để thực hiện tác vụ");
            return;
        }

        if (!selected.getStatus().equals("Đang chờ")) {
            App.showAlert(Alert.AlertType.WARNING, "Chỉ có thể từ chối yêu cầu đang chờ", "");
            return;
        }

        // Decline request and reload list
        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);

        regConfDao.declineReq(selected.getIdUser(), selected.getIdConference());
        requestsObservableList = FXCollections.observableList(regConfDao.selectByConference(this.idConference));
        numberOfRegistrations.setText(String.valueOf(regConfDao.countRegConf(this.idConference)));

        entityManager.close();

        requestsTableView.setItems(requestsObservableList);
        App.showAlert(Alert.AlertType.INFORMATION, "1 Yêu cầu tham dự đã bị từ chối", "");
    }
}
