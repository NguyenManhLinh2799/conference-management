package controllers.user;

import controllers.ConferenceInfoController;
import daos.ConferenceDao;
import daos.RegConfDao;
import entities.Conference;
import entities.RegisterConference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConferenceStatController implements Initializable {
    private String keywords = "";
    private String searchBy = "Tên";
    private String time = "Tất cả thời gian";
    private String status = "Tất cả tình trạng";

    // Search and filter properties
    @FXML
    private TextField keywordsField;
    @FXML
    private ChoiceBox<String> searchByChoice;
    @FXML
    private ChoiceBox<String> timeChoice;
    @FXML
    private ChoiceBox<String> statusChoice;

    // Table properties
    @FXML
    private TableView<RegisterConference> regConfTableView;
    @FXML
    private TableColumn<RegisterConference, Integer> idColumn;
    @FXML
    private TableColumn<RegisterConference, String> nameColumn;
    @FXML
    private TableColumn<RegisterConference, String> summaryColumn;
    @FXML
    private TableColumn<RegisterConference, Integer> locationColumn;
    @FXML
    private TableColumn<RegisterConference, String> timeColumn;
    @FXML
    private TableColumn<RegisterConference, String> statusColumn;

    private ObservableList<RegisterConference> regConfObservableList;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        keywordsField.setText(keywords);

        searchByChoice.getItems().addAll("Tên", "Mô tả", "Địa điểm");
        searchByChoice.setValue(searchBy);

        timeChoice.getItems().addAll("Tất cả thời gian", "Đã diễn ra", "Chưa diễn ra");
        timeChoice.setValue(time);

        statusChoice.getItems().addAll("Tất cả tình trạng", "Đang chờ", "Chấp nhận");
        statusChoice.setValue(status);

        // Select all conferences that user registered
        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);
        regConfObservableList = FXCollections.observableList(regConfDao.selectByUser(App.getUser().getId()));
        entityManager.close();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("idConference"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nameConference"));
        summaryColumn.setCellValueFactory(new PropertyValueFactory<>("summaryConference"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationConference"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("timeConference"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        regConfTableView.setItems(regConfObservableList);
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("primary"));
        stage.setScene(scene);
    }

    public void search() {
        keywords = keywordsField.getText();
        searchBy = searchByChoice.getValue();
        time = timeChoice.getValue();
        status = statusChoice.getValue();

        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);
        regConfObservableList = FXCollections.observableList(
                regConfDao.searchByUser(App.getUser().getId(), keywords, searchBy, time, status));
        regConfTableView.setItems(regConfObservableList);
        entityManager.close();
    }

    public void onRowSelected(MouseEvent e) throws IOException, URISyntaxException {
        RegisterConference selected = regConfTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

            // Load fxml
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("conferenceInfo.fxml"));
            Parent conferenceInfoParent = fxmlLoader.load();
            Scene scene = new Scene(conferenceInfoParent);

            // Find conference by id
            EntityManager entityManager = App.createEntityManager();
            Conference conference = (new ConferenceDao(entityManager)).find(selected.getIdConference());
            entityManager.close();

            // Set conference info
            ConferenceInfoController conferenceInfoController = fxmlLoader.getController();
            conferenceInfoController.setConference(conference);
            conferenceInfoController.setPrevious("conferenceStat");

            stage.setScene(scene);
        }
    }
}
