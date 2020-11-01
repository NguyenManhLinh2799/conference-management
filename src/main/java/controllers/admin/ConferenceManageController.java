package controllers.admin;

import daos.ConferenceDao;
import entities.Conference;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ConferenceManageController implements Initializable {
    // Conference table properties
    @FXML
    private TableView<Conference> conferenceTableView;
    @FXML
    private TableColumn<Conference, Integer> idColumn;
    @FXML
    private TableColumn<Conference, String> nameColumn;
    @FXML
    private TableColumn<Conference, String> summaryColumn;
    @FXML
    private TableColumn<Conference, Integer> locationColumn;
    @FXML
    private TableColumn<Conference, String> timeColumn;
    @FXML
    private TableColumn<Conference, Integer> numberOfAttendeesColumn;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Row details
        conferenceTableView.setRowFactory(tv -> new TableRow<>() {
            Node detailsPane;
            {
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

        //======================================================================================================
        // Set list
        EntityManager entityManager = App.createEntityManager();
        ConferenceDao conferenceDao = new ConferenceDao(entityManager);
        ObservableList<Conference> conferenceObservableList = FXCollections.observableList(conferenceDao.selectAll());
        entityManager.close();

        // Set columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        summaryColumn.setCellValueFactory(new PropertyValueFactory<>("summary"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("locationName"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("dateOnly"));
        numberOfAttendeesColumn.setCellValueFactory(new PropertyValueFactory<>("numberOfAttendees"));
        conferenceTableView.setItems(conferenceObservableList);
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("primary"));
        stage.setScene(scene);
    }

    public void newConference(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("newConference"));
        stage.setScene(scene);
    }

    private Node createDetailsPane(ObjectProperty<Conference> item) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("detailsPane.fxml"));
        Node detailsPane = fxmlLoader.load();

        DetailsPaneController controller = fxmlLoader.getController();
        item.addListener((obs, oldItem, newItem) -> controller.setConference(newItem));

        return detailsPane;
    }
}
