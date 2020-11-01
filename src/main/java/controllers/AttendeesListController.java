package controllers;

import daos.RegConfDao;
import entities.RegisterConference;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;

public class AttendeesListController {
    @FXML
    private TableView<RegisterConference> attendeesTableView;
    @FXML
    private TableColumn<RegisterConference, Integer> numberCol;
    @FXML
    private TableColumn<RegisterConference, String> fullnameCol;
    @FXML
    private TableColumn<RegisterConference, String> emailCol;

    public void setAttendees(int idConference)
    {
        EntityManager entityManager = App.createEntityManager();
        RegConfDao regConfDao = new RegConfDao(entityManager);
        ObservableList<RegisterConference> attendeesObservableList = FXCollections.observableList(regConfDao.selectAttendees(idConference));
        entityManager.close();

        // Set auto increment column
        numberCol.setCellValueFactory(regConfIntegerCellDataFeatures ->
                new ReadOnlyObjectWrapper(
                        attendeesTableView.getItems().indexOf(regConfIntegerCellDataFeatures.getValue()) + 1
                )
        );
        numberCol.setSortable(false);
        fullnameCol.setCellValueFactory(new PropertyValueFactory<>("fullname"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        attendeesTableView.setItems(attendeesObservableList);
    }
}
