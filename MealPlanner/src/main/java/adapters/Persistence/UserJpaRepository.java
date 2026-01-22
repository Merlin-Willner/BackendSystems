package adapters.Persistence;

import application.port.out.UserRepository;
import domain.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserJpaRepository implements UserRepository{

    @Inject
    EntityManager entityManager;

    //entscheide  zwischne neu oder update
    @Override
    @Transactional
    public User save(User user) {
        if (user.getUserId() == null) {
            entityManager.persist(user);
            entityManager.flush();
            return user;
        } else {
            return entityManager.merge(user);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(entityManager.find(User.class, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        TypedQuery<User> query = entityManager.createQuery(
                "SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }

    @Override
    @Transactional
    public void delete(User user) {
        User managed = entityManager.contains(user) ? user : entityManager.merge(user);
        entityManager.remove(managed);
    }

}
