package daos;

import entities.Location;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class LocationDao {
    private final EntityManager entityManager;
    private final EntityTransaction entityTransaction;

    public LocationDao(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityTransaction = this.entityManager.getTransaction();
    }

    // Create new location
    public void persist(String name, String address, int capacity) {
        beginTransaction();
        Location newLocation = new Location(name, address, capacity);
        entityManager.persist(newLocation);
        commitTransaction();
    }

    // Find by id
    public Location find(int id) {
        return entityManager.find(Location.class, id);
    }

    // Find by name
    public Location findByName(String name) {
        Location result = null;
        Query query = entityManager.createQuery("SELECT l FROM Location l WHERE l.name = '" + name + "'");
        try {
            result = (Location) query.getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Select all locations
    public List<Location> selectAll() {
        List<Location> result = null;
        Query query = entityManager.createQuery("SELECT l FROM Location l");
        try {
            result = (List<Location>) query.getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Update location
    public void update(int id, String name, String address, int capacity) {
        beginTransaction();
        Location location = entityManager.find(Location.class, id);
        location.setName(name);
        location.setAddress(address);
        location.setCapacity(capacity);
        entityManager.merge(location);
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
