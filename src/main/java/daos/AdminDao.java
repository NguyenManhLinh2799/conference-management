package daos;

import entities.Admin;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class AdminDao {
    private final EntityManager entityManager;
    private final EntityTransaction entityTransaction;

    public AdminDao(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityTransaction = this.entityManager.getTransaction();
    }

    // Create new admin
    public void persist(String fullname, String username, String password, String email) {
        beginTransaction();
        Admin newAdmin = new Admin(fullname, username, password, email);
        entityManager.persist(newAdmin);
        commitTransaction();
    }

    // Find admin by id
    public Admin find(int id) {
        return entityManager.find(Admin.class, id);
    }

    // Find admin by username
    public Admin findByUsername(String username) {
        Admin result = null;
        Query query = entityManager.createQuery("SELECT a FROM Admin a WHERE a.username = '" + username + "'");
        try {
            result = (Admin) query.getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Find admin by email
    public Admin findByEmail(String email) {
        Admin result = null;
        Query query = entityManager.createQuery("SELECT a FROM Admin a WHERE a.email = '" + email + "'");
        try {
            result = (Admin) query.getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Update admin
    public void update(int id, String fullname, String email) {
        beginTransaction();
        Admin admin = entityManager.find(Admin.class, id);
        admin.setFullname(fullname);
        admin.setEmail(email);
        entityManager.merge(admin);
        commitTransaction();
    }

    // Change password
    public void changePassword(int idAdmin, String newPassword) {
        beginTransaction();
        Admin admin = entityManager.find(Admin.class, idAdmin);
        admin.setPassword(newPassword);
        entityManager.merge(admin);
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
