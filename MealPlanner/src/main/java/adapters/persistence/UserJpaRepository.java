package adapters.persistence;

import adapters.persistence.entity.UserEntity;
import adapters.persistence.mapper.PersistenceMapper;
import application.exception.ConcurrencyException;
import application.exception.ConflictException;
import application.port.out.UserRepository;
import domain.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Optional;

@ApplicationScoped
public class UserJpaRepository implements UserRepository{

    @Inject
    EntityManager entityManager;

    //entscheide  zwischne neu oder update
    @Override
    @Transactional
    public User save(User user) {
        try {
            if (user.getUserId() == null) {
                UserEntity entity = new UserEntity();
                PersistenceMapper.updateUserEntity(entity, user);
                entityManager.persist(entity);
                entityManager.flush();
                return PersistenceMapper.toDomain(entity);
            }
            UserEntity existing = entityManager.find(UserEntity.class, user.getUserId());
            if (existing == null) {
                UserEntity entity = new UserEntity();
                PersistenceMapper.updateUserEntity(entity, user);
                entityManager.persist(entity);
                entityManager.flush();
                return PersistenceMapper.toDomain(entity);
            }
            PersistenceMapper.updateUserEntity(existing, user);
            return PersistenceMapper.toDomain(existing);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Username oder Email existiert bereits");
            }
            throw new RuntimeException("Persistence error", e);
        }
    }

    @Override
    public Optional<User> findById(long id) {
        UserEntity entity = entityManager.find(UserEntity.class, id);
        return Optional.ofNullable(PersistenceMapper.toDomain(entity));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        TypedQuery<UserEntity> query = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.email = :email", UserEntity.class);
        query.setParameter("email", email);
        return query.getResultStream()
                .map(PersistenceMapper::toDomain)
                .findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        TypedQuery<UserEntity> query = entityManager.createQuery(
                "SELECT u FROM UserEntity u WHERE u.username = :username", UserEntity.class);
        query.setParameter("username", username);
        return query.getResultStream()
                .map(PersistenceMapper::toDomain)
                .findFirst();
    }

    @Override
    @Transactional
    public void delete(User user) {
        try {
            if (user == null || user.getUserId() == null) {
                return;
            }
            UserEntity managed = entityManager.find(UserEntity.class, user.getUserId());
            if (managed != null) {
                entityManager.remove(managed);
            }
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            throw new RuntimeException("Persistence error", e);
        }
    }

    private boolean isConstraintViolation(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof ConstraintViolationException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
