package controllers.admin;

import daos.ConferenceDao;
import daos.LocationDao;
import entities.Conference;
import entities.Location;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.ResourceBundle;

public class NewConferenceController implements Initializable {
    private File imageFile = null;

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

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Get all location
        EntityManager entityManager = App.createEntityManager();
        LocationDao locationDao = new LocationDao(entityManager);
        loc.getItems().addAll(locationDao.selectAll());
        entityManager.close();

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
        datePicker.setValue(convertToLocalDate(new Date()));

        // Set default image
        Image image = null;
        try {
            image = new Image(
                    App.class.getResource("/images/conference.png").toURI().toString(),
                    200, 150, false, true);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        imageView.setImage(image);
    }

    public void goBack(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("conferenceManage"));
        stage.setScene(scene);
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

    public void create(ActionEvent e) throws IOException, ParseException {
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
        int noAttendees;
        try {
            noAttendees = Integer.parseInt(numberOfAttendees.getText());

        } catch (NumberFormatException numberFormatException) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Số người tham dự không hợp lệ",
                    "Vui lòng nhập lại số người tham dự");
            return;
        }
        if (noAttendees <= 0) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Số người tham dự phải là số nguyên dương",
                    "Vui lòng nhập lại số người tham dự");
            return;
        }
        if (noAttendees > loc.getValue().getCapacity()) {
            App.showAlert(Alert.AlertType.ERROR,
                    "Số người tham dự đã vượt quá sức chứa",
                    "Vui lòng nhập lại số người tham dự");
            return;
        }

        EntityManager entityManager = App.createEntityManager();
        ConferenceDao conferenceDao = new ConferenceDao(entityManager);

        // Check if there's a similar name
        Conference findName = conferenceDao.findByName(name.getText());
        if (findName != null) {
            App.showAlert(Alert.AlertType.WARNING,
                    "Tên Hội nghị '" + name.getText() + "' đã bị trùng",
                    "Vui lòng nhập tên khác");
            entityManager.close();
            return;
        }

        // Check if location already used in that day
        Conference findByLocationAndTime = conferenceDao.findByLocationAndTime(loc.getValue(), time);
        if (findByLocationAndTime != null) {
            App.showAlert(Alert.AlertType.WARNING,
                    "Địa điểm '" + loc.getValue().getName() + "' đã được sử dụng vào ngày " + time,
                    "Vui lòng nhập lại thời gian hoặc địa điểm");
            entityManager.close();
            return;
        }

        // Set image file name
        String imgFileName;
        if (imageFile == null) {
            imgFileName = "conference.png"; // Default
        } else {
            imgFileName = imageFile.getName().replaceAll("\\s", "-");
            // Copy image to resources
            try {
                Path src = imageFile.toPath();

                String desPath = App.class.getResource("/images/") + imgFileName;
                Path des = Paths.get(URI.create(desPath));

                Files.copy(src, des, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception exception) {
                App.showAlert(Alert.AlertType.ERROR, "Đã có lỗi xảy ra", "Đường dẫn tập tin không hợp lệ");
                return;
            }
        }

        // Persist new conference
        conferenceDao.persist(
                name.getText(),
                summary.getText(),
                detail.getText(),
                imgFileName,
                time,
                loc.getValue(),
                Integer.parseInt(numberOfAttendees.getText())
        );

        entityManager.close();

        // Go back to conference manage
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        Scene scene = new Scene(App.loadFXML("conferenceManage"));
        stage.setScene(scene);
        App.showAlert(Alert.AlertType.INFORMATION, "1 Hội nghị mới đã được thêm vào danh sách", "");
    }

    public boolean isEmptyAnyField() {
        return name.getText().equals("")
                || loc.getValue() == null
                || numberOfAttendees.getText().equals("")
                || summary.getText().equals("")
                || detail.getText().equals("");
    }

    public void showLocationManage(ActionEvent e) throws IOException {
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();

        // Load fxml
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("locationManage.fxml"));
        Parent locationManageParent = fxmlLoader.load();
        Scene scene = new Scene(locationManageParent);

        // Set conference info
        LocationManageController locationManageController = fxmlLoader.getController();
        locationManageController.setPrevious("newConference");

        stage.setScene(scene);
    }

    public LocalDate convertToLocalDate(Date dateToConvert) {
        return LocalDate.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault());
    }

    public Date convertToDateViaSqlDate(LocalDate dateToConvert) {
        return java.sql.Date.valueOf(dateToConvert);
    }
}
