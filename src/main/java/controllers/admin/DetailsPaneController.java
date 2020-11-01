package controllers.admin;

import daos.ConferenceDao;
import daos.LocationDao;
import daos.RegConfDao;
import entities.Conference;
import entities.Location;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DetailsPaneController {
    private Conference conference;
    private File imageFile = null;

    @FXML
    private Label id;
    @FXML
    private TextField name;
    @FXML
    private TextArea summary;
    @FXML
    private TextArea detail;
    @FXML
    private ImageView imageView;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ChoiceBox<Location> loc;
    @FXML
    private TextField numberOfAttendees;

    public void setConference(Conference conference) {
        if (conference != null) {
            this.conference = conference;

            id.setText(String.valueOf(conference.getId()));
            name.setText(conference.getName());
            summary.setText(conference.getSummary());
            detail.setText(conference.getDetail());

            // Set date picker
            datePicker.setConverter(new StringConverter<>() {
                private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                @Override
                public String toString(LocalDate localDate) {
                    if (localDate == null)
                        return "";
                    return dateTimeFormatter.format(localDate);
                }

                @Override
                public LocalDate fromString(String dateString) {
                    if (dateString == null || dateString.trim().isEmpty()) {
                        return null;
                    }
                    return LocalDate.parse(dateString, dateTimeFormatter);
                }
            });
            datePicker.setValue(convertToLocalDate(conference.getTime()));

            // Set location
            EntityManager entityManager = App.createEntityManager();
            LocationDao locationDao = new LocationDao(entityManager);
            loc.getItems().addAll(locationDao.selectAll());
            loc.setValue(locationDao.find(conference.getLocation().getId()));

            numberOfAttendees.setText(String.valueOf(conference.getNumberOfAttendees()));

            // Set image
            Image image = null;
            try {
                image = new Image(
                        App.class.getResource("/images/" + conference.getImage()).toURI().toString(),
                        200, 150, false, true);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            imageView.setImage(image);

            entityManager.close();
        }
    }

    public void chooseImage(ActionEvent e) {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn một tấm ảnh đại diện");
        FileChooser.ExtensionFilter imageFilter = new FileChooser.ExtensionFilter("Tập tin hình ảnh", "*.jpg", "*.png");
        fc.getExtensionFilters().add(imageFilter);
        File file = fc.showOpenDialog(stage);

        imageFile = file;

        Image image = null;
        try {
            image = new Image(file.toURI().toString(), 200, 150, false, true);
        } catch (NullPointerException ignored) {

        }
        if (image != null) {
            imageView.setImage(image);
        }
    }

    public void save(ActionEvent e) throws IOException {
        if (isEmptyAnyField()) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Thiếu thông tin",
                    "Vui lòng điền đầy đủ các trường thông tin");
            return;
        }

        // Date validation
        Date time = convertToDateViaSqlDate(datePicker.getValue());
        // Check if time is in the past
        if (time.before(new Date())) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Không được đặt mốc thời gian ở quá khứ",
                    "Vui lòng nhập lại, trừ khi bạn có cỗ máy thời gian");
            return;
        }

        // Validate number of attendees
        if (!validateNoAttendees()) {
            return;
        }

        EntityManager entityManager = App.createEntityManager();
        ConferenceDao conferenceDao = new ConferenceDao(entityManager);

        // Check if there's a similar name
        Conference findName = conferenceDao.findByName(name.getText());
        if (findName != null && findName.getId() != conference.getId()) {
            App.showAlert(Alert.AlertType.WARNING,
                    "Tên Hội nghị '" + name.getText() + "' đã bị trùng",
                    "Vui lòng nhập tên khác");
            entityManager.close();
            return;
        }

        // Check if location already used in that day
        Conference findByLocationAndTime = conferenceDao.findByLocationAndTime(loc.getValue(), time);
        if (findByLocationAndTime != null && findByLocationAndTime.getId() != conference.getId()) {
            App.showAlert(Alert.AlertType.WARNING,
                    "Địa điểm '" + loc.getValue().getName() + "' đã được sử dụng vào ngày " + time,
                    "Vui lòng nhập lại thời gian hoặc địa điểm");
            entityManager.close();
            return;
        }

        // Copy image to resources if new
        String image;
        if (imageFile != null) {
            Path src = imageFile.toPath();

            image = imageFile.getName().replaceAll("\\s", "-");
            String desPath = App.class.getResource("/images/") + image;
            Path des = Paths.get(URI.create(desPath));

            Files.copy(src, des, StandardCopyOption.REPLACE_EXISTING);
        } else {
            image = conference.getImage();
        }

        // Update
        conferenceDao.update(
                conference.getId(),
                name.getText(),
                summary.getText(),
                detail.getText(),
                image,
                time,
                loc.getValue(),
                Integer.parseInt(numberOfAttendees.getText())
        );

        entityManager.close();

        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("conferenceManage"));
        stage.setScene(scene);
        App.showAlert(Alert.AlertType.INFORMATION, "Cập nhật thành công", "");
    }

    public boolean isEmptyAnyField() {
        return name.getText().equals("")
                || numberOfAttendees.getText().equals("")
                || summary.getText().equals("")
                || detail.getText().equals("");
    }

    public boolean validateNoAttendees() {
        int noAttendees;
        // It must be an integer
        try {
            noAttendees = Integer.parseInt(numberOfAttendees.getText());
        } catch (NumberFormatException numberFormatException) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Số người tham dự không hợp lệ",
                    "Vui lòng nhập lại số người tham dự");
            return false;
        }
        // It must be a positive number
        if (noAttendees <= 0) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Số người tham dự phải là số nguyên dương",
                    "Vui lòng nhập lại số người tham dự");
            return false;
        }
        // It must not be greater than capacity
        if (noAttendees > loc.getValue().getCapacity()) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Số người tham dự không được lớn hơn sức chứa",
                    "Vui lòng nhập lại số người tham dự");
            return false;
        }
        // It must not be less than number of registration
        EntityManager entityManager = App.createEntityManager();
        int numberOfRegistrations = (new RegConfDao(entityManager)).countRegConf(conference.getId());
        if (noAttendees < numberOfRegistrations) {
            App.showAlert(Alert.AlertType.ERROR, "Số người tham dự không được nhỏ hơn số lượng đăng ký",
                    "Xem danh sách đăng ký tham dự để biết thêm chi tiết");
            entityManager.close();
            return false;
        }
        entityManager.close();
        return true;
    }

    public void showRequestsList() throws IOException {
        Dialog<Object> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("requestsList.fxml"));
        Parent requestsListParent = fxmlLoader.load();
        RequestsListController requestsListController = fxmlLoader.getController();
        requestsListController.setRequests(conference.getId());

        dialog.getDialogPane().setContent(requestsListParent);
        dialog.show();
    }

    public void showLocationManage(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("locationManage.fxml"));
        Parent locationManageParent = fxmlLoader.load();
        Scene scene = new Scene(locationManageParent);

        // Set conference info
        LocationManageController locationManageController = fxmlLoader.getController();
        locationManageController.setPrevious("conferenceManage");

        stage.setScene(scene);
    }

    public LocalDate convertToLocalDate(Date dateToConvert) {
        return LocalDate.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault());
    }

    public Date convertToDateViaSqlDate(LocalDate dateToConvert) {
        return java.sql.Date.valueOf(dateToConvert);
    }
}
