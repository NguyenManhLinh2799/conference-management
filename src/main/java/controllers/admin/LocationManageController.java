package controllers.admin;

import daos.LocationDao;
import entities.Location;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LocationManageController implements Initializable {
    private String previous;

    // Table properties
    @FXML
    private TableView<Location> locationTableView;
    @FXML
    private TableColumn<Location, Integer> idCol;
    @FXML
    private TableColumn<Location, String> nameCol;
    @FXML
    private TableColumn<Location, String> addressCol;
    @FXML
    private TableColumn<Location, Integer> capacityCol;

    // Update
    @FXML
    private Label idLocation;
    @FXML
    private TextField updateNameField;
    @FXML
    private TextArea updateAddressArea;
    @FXML
    private TextField updateCapacityField;
    @FXML
    private Button saveBtn;

    // New
    @FXML
    private TextField newNameField;
    @FXML
    private TextArea newAddressArea;
    @FXML
    private TextField newCapacityField;

    private ObservableList<Location> locationObservableList;

    public void setPrevious(String previous) {
        this.previous = previous;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        EntityManager entityManager = App.createEntityManager();
        locationObservableList = FXCollections.observableList((new LocationDao(entityManager)).selectAll());
        entityManager.close();

        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        locationTableView.setItems(locationObservableList);
    }

    public void onRowSelected() {
        Location selected = locationTableView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            idLocation.setText(String.valueOf(selected.getId()));
            updateNameField.setText(selected.getName());
            updateAddressArea.setText(selected.getAddress());
            updateCapacityField.setText(String.valueOf(selected.getCapacity()));
            saveBtn.setDisable(false);
        }
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML(previous));
        stage.setScene(scene);
    }

    public void save() {
        int id = Integer.parseInt(idLocation.getText());
        String updateName = updateNameField.getText();
        String updateAddress = updateAddressArea.getText();
        String updateCapacity = updateCapacityField.getText();

        if (updateName.equals("") || updateAddress.equals("") || updateCapacity.equals("")) {
            App.showAlert(Alert.AlertType.ERROR, "Thiếu thông tin",
                    "Vui lòng nhập đầy đủ các trường thông tin");
            return;
        }

        // Validate capacity
        int capacity;
        try {
            capacity = Integer.parseInt(updateCapacity);
        } catch (NumberFormatException e) {
            App.showAlert(Alert.AlertType.ERROR, "Sức chứa không hợp lệ",
                    "Vui lòng nhập lại sức chứa");
            return;
        }
        if (capacity <= 0) {
            App.showAlert(Alert.AlertType.ERROR, "Sức chứa phải là một số nguyên dương",
                    "Vui lòng nhập lại sức chứa");
            return;
        }

        // Check if there's a similar name
        EntityManager entityManager = App.createEntityManager();
        LocationDao locationDao = new LocationDao(entityManager);

        Location findName = locationDao.findByName(updateName);
        if (findName != null && findName.getId() != id) {
            App.showAlert(Alert.AlertType.ERROR, "Tên địa điểm '" + updateName + "' đã bị trùng",
                    "Vui lòng nhập tên khác");
            entityManager.close();
            return;
        }

        // Update location and list
        locationDao.update(id, updateName, updateAddress, capacity);
        locationObservableList = FXCollections.observableList(locationDao.selectAll());

        entityManager.close();

        locationTableView.setItems(locationObservableList);
        App.showAlert(Alert.AlertType.INFORMATION, "Cập nhật thành công", "");
    }
    
    public void create()
    {
        String newName = newNameField.getText();
        String newAddress = newAddressArea.getText();
        String newCapacity = newCapacityField.getText();

        if (newName.equals("") || newAddress.equals("") || newCapacity.equals("")) {
            App.showAlert(Alert.AlertType.ERROR, "Thiếu thông tin",
                    "Vui lòng nhập đầy đủ các trường thông tin");
            return;
        }

        // Validate capacity
        int capacity;
        try {
            capacity = Integer.parseInt(newCapacity);
        } catch (NumberFormatException e) {
            App.showAlert(Alert.AlertType.ERROR, "Sức chứa không hợp lệ",
                    "Vui lòng nhập lại sức chứa");
            return;
        }
        if (capacity <= 0) {
            App.showAlert(Alert.AlertType.ERROR, "Sức chứa phải là một số nguyên dương",
                    "Vui lòng nhập lại sức chứa");
            return;
        }

        // Check if there's a similar name
        EntityManager entityManager = App.createEntityManager();
        LocationDao locationDao = new LocationDao(entityManager);

        Location findName = locationDao.findByName(newName);
        if (findName != null) {
            App.showAlert(Alert.AlertType.ERROR, "Tên địa điểm '" + newName + "' đã bị trùng",
                    "Vui lòng nhập tên khác");
            entityManager.close();
            return;
        }

        // Persist new location and update list
        locationDao.persist(newName, newAddress, capacity);
        locationObservableList = FXCollections.observableList(locationDao.selectAll());

        entityManager.close();

        locationTableView.setItems(locationObservableList);
        App.showAlert(Alert.AlertType.INFORMATION, "1 địa điểm mới đã được thêm vào danh sách", "");
    }
}
