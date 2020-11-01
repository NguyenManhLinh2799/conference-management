package controllers.admin;

import daos.RegConfDao;
import entities.RegisterConference;
import entities.User;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import org.nmlinh2799.App;

import javax.persistence.EntityManager;
import java.util.List;

public class ConferencesListPaneController {
    @FXML
    private ListView<String> listView;

    public void setListView(User user) {
        if (user == null) return;

        EntityManager entityManager = App.createEntityManager();
        List<RegisterConference> list = (new RegConfDao(entityManager)).selectByUserAccepted(user.getId());
        entityManager.close();

        if (list == null) return;

        for (RegisterConference rc : list) {
            String item = "ID: " + rc.getIdConference() + " - " + rc.getConference().getName() + " - " + rc.getConference().getSummary();
            listView.getItems().add(item);
        }
    }
}
