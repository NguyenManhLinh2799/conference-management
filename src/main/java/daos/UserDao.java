package daos;

import entities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class UserDao {
    private final EntityManager entityManager;
    private final EntityTransaction entityTransaction;

    public UserDao(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.entityTransaction = this.entityManager.getTransaction();
    }

    // Create new user
    public void persist(String fullname, String username, String password, String email) {
        beginTransaction();
        User newUser = new User(fullname, username, password, email);
        entityManager.persist(newUser);
        commitTransaction();
    }

    // Select all users
    public List<User> selectAll() {
        List<User> result = null;
        try {
            result = (List<User>) entityManager.createQuery("SELECT u FROM User u").getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Find user by id
    public User find(int id) {
        return entityManager.find(User.class, id);
    }

    // Find user by username
    public User findByUsername(String username) {
        User result = null;
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.username = '" + username + "'");
        try {
            result = (User) query.getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Find user by email
    public User findByEmail(String email) {
        User result = null;
        Query query = entityManager.createQuery("SELECT u FROM User u WHERE u.email = '" + email + "'");
        try {
            result = (User) query.getSingleResult();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Search
    public List<User> search(String keywords, String searchBy, String status) {
        List<User> result = null;
        String queryStr = "SELECT u FROM User u";
        if (searchBy.equals("Họ tên")) {
            queryStr += " WHERE u.fullname LIKE '%" + keywords + "%'";
        } else if (searchBy.equals("Tên tài khoản")) {
            queryStr += " WHERE u.username LIKE '%" + keywords + "%'";
        } else {
            queryStr += " WHERE u.email LIKE '%" + keywords + "%'";
        }

        if (status.equals("Bị chặn")) {
            queryStr += " AND u.blocked = true";
        } else if (status.equals("Không bị chặn")) {
            queryStr += " AND u.blocked = false";
        }

        try {
            result = (List<User>) entityManager.createQuery(queryStr).getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }

    // Update user
    public void update(int id, String fullname, String email) {
        beginTransaction();
        User user = entityManager.find(User.class, id);
        user.setFullname(fullname);
        user.setEmail(email);
        entityManager.merge(user);
        commitTransaction();
    }

    // Block
    public void block(int idUser) {
        beginTransaction();
        User user = entityManager.find(User.class, idUser);
        user.setBlocked(true);
        entityManager.merge(user);
        commitTransaction();
    }

    // Unblock
    public void unblock(int idUser) {
        beginTransaction();
        User user = entityManager.find(User.class, idUser);
        user.setBlocked(false);
        entityManager.merge(user);
        commitTransaction();
    }

    // Change password
    public void changePassword(int idUser, String newPassword) {
        beginTransaction();
        User user = entityManager.find(User.class, idUser);
        user.setPassword(newPassword);
        entityManager.merge(user);
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
