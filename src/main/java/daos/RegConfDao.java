package daos;

import entities.RegConfId;
import entities.RegisterConference;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class RegConfDao {
    private final EntityManager entityManager;
    private final EntityTransaction entityTransaction;

    public RegConfDao(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityTransaction = this.entityManager.getTransaction();
    }

    // Create new registration
    public void persist(int idUser, int idConference) {
        beginTransaction();
        RegisterConference newRegistration = new RegisterConference(idUser, idConference);
        entityManager.persist(newRegistration);
        commitTransaction();
    }

    // Find registration by primary keys
    public RegisterConference find(int idUser, int idConference) {
        return entityManager.find(RegisterConference.class, new RegConfId(idUser, idConference));
    }

    // Count all registrations of conference
    public int countRegConf(int idConference) {
        Query query = entityManager.createQuery("SELECT rc FROM RegisterConference rc" +
                " WHERE rc.idConference = '" + idConference + "'");
        return query.getResultList().size();
    }

    // Select by idUser
    public List<RegisterConference> selectByUser(int idUser) {
        List<RegisterConference> result = null;
        Query query = entityManager.createQuery(
                "Select rc FROM RegisterConference rc WHERE rc.idUser = " + idUser);
        try {
            result = (List<RegisterConference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Select by idUser that is accepted
    public List<RegisterConference> selectByUserAccepted(int idUser) {
        List<RegisterConference> result = null;
        Query query = entityManager.createQuery(
                "Select rc FROM RegisterConference rc" +
                        " WHERE rc.idUser = " + idUser +
                        " AND rc.status = 'Chấp nhận'");
        try {
            result = (List<RegisterConference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Select by idConference
    public List<RegisterConference> selectByConference(int idConference) {
        List<RegisterConference> result = null;
        Query query = entityManager.createQuery(
                "Select rc FROM RegisterConference rc WHERE rc.idConference = " + idConference);
        try {
            result = (List<RegisterConference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Search by idUser
    public List<RegisterConference> searchByUser(int idUser, String keywords, String searchBy, String time, String status) {
        List<RegisterConference> result = null;

        String column;
        if (searchBy.equals("Tên")) {
            column = "name";
        } else if (searchBy.equals("Mô tả")) {
            column = "summary";
        } else {
            column = "location.name";
        }

        String queryString = "Select rc" +
                " FROM RegisterConference rc" +
                " WHERE rc.idUser = " + idUser +
                " AND rc.conference." + column + " LIKE '%" + keywords + "%'";

        String today = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
        if (time.equals("Chưa diễn ra")) {
            queryString += " AND rc.conference.time > '" + today + "'";
        } else if (time.equals("Đã diễn ra")) {
            queryString += " AND rc.conference.time < '" + today + "'";
        }

        if (!status.equals("Tất cả tình trạng")) {
            queryString += " And rc.status = '" + status + "'";
        }

        Query query = entityManager.createQuery(queryString);

        try {
            result = (List<RegisterConference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Accept request
    public void acceptReq(int idUser, int idConference) {
        beginTransaction();
        RegisterConference registerConference = find(idUser, idConference);
        registerConference.setStatus("Chấp nhận");
        entityManager.merge(registerConference);
        commitTransaction();
    }

    // Decline request
    public void declineReq(int idUser, int idConference) {
        beginTransaction();
        RegisterConference registerConference = find(idUser, idConference);
        entityManager.remove(registerConference);
        commitTransaction();
    }

    // Select attendees by idConference
    public List<RegisterConference> selectAttendees(int idConference) {
        List<RegisterConference> result = null;
        Query query = entityManager.createQuery("SELECT rc FROM RegisterConference rc" +
                " WHERE rc.idConference = " + idConference +
                " AND rc.status = 'Chấp nhận'");
        try {
            result = (List<RegisterConference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Cancel registration
    public void cancelRegistration(int idUser, int idConference) {
        beginTransaction();
        RegisterConference registerConference = find(idUser, idConference);
        entityManager.remove(registerConference);
        commitTransaction();
    }

    private void beginTransaction() {
        try {
            entityTransaction.begin();
        } catch (IllegalStateException e) {
            rollbackTransaction();
        }
    }

    private void commitTransaction() {
        try {
            entityTransaction.commit();
        } catch (IllegalStateException e) {
            rollbackTransaction();
        }
    }

    private void rollbackTransaction() {
        try {
            entityTransaction.rollback();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
