package daos;

import entities.Conference;
import entities.Location;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ConferenceDao {
    private final EntityManager entityManager;
    private final EntityTransaction entityTransaction;

    public ConferenceDao(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityTransaction = this.entityManager.getTransaction();
    }

    // Find by id
    public Conference find(int id) {
        return entityManager.find(Conference.class, id);
    }

    // Create new conference
    public void persist(String name, String summary, String detail, String image, Date time, Location location, int numberOfAttendees) throws ParseException {
        beginTransaction();

        Conference newConference = new Conference(
                name,
                summary,
                detail,
                image,
                time,
                location,
                numberOfAttendees
        );

        entityManager.merge(newConference);
        commitTransaction();
    }

    // Select all conferences
    public List<Conference> selectAll() {
        List<Conference> result = null;
        Query query = entityManager.createQuery("SELECT c FROM Conference c");
        try {
            result = (List<Conference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Filter by time
    public List<Conference> filterByTime(String time) {
        List<Conference> result = null;

        String queryString = "Select c FROM Conference c";
        String today = (new SimpleDateFormat("yyyy-MM-dd")).format(new Date());
        if (time.equals("Chưa diễn ra")) {
            queryString += " WHERE c.time > '" + today + "'";
        } else if (time.equals("Đã diễn ra")) {
            queryString += " WHERE c.time < '" + today + "'";
        }

        Query query = entityManager.createQuery(queryString);

        try {
            result = (List<Conference>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Find by name
    public Conference findByName(String name) {
        Conference result = null;
        Query query = entityManager.createQuery("SELECT c FROM Conference c WHERE c.name = '" + name + "'");
        try {
            result = (Conference) query.getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Find by location and date
    public Conference findByLocationAndTime(Location location, Date time) {
        String dbTime = (new SimpleDateFormat("yyyy-MM-dd").format(time));
        int idLocation = location.getId();
        Conference result = null;
        try {
            result = (Conference) entityManager.createQuery("SELECT c FROM Conference c" +
                    " WHERE c.location.id = " + idLocation +
                    " AND c.time = '" + dbTime + "'").getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Update conference
    public void update(int id, String name, String summary, String detail, String image, Date time, Location location, int numberOfAttendees) {
        beginTransaction();

        Conference conference = entityManager.find(Conference.class, id);
        conference.setName(name);
        conference.setSummary(summary);
        conference.setDetail(detail);
        conference.setImage(image);
        conference.setTime(time);
        conference.setLocation(location);
        conference.setNumberOfAttendees(numberOfAttendees);

        entityManager.merge(conference);
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
