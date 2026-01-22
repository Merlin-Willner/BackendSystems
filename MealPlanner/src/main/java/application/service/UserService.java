package application.service;


import application.exception.ConflictException;
import application.exception.ConcurrencyException;
import application.exception.NotFoundException;
import application.port.in.UserAPI;
import application.port.out.ShoppingCartRepository;
import application.port.out.UserRepository;
import domain.entity.ShoppingCart;
import domain.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.PersistenceException;
import jakarta.transaction.Transactional;
import org.hibernate.exception.ConstraintViolationException;

import java.util.Optional;

@ApplicationScoped
public class UserService implements UserAPI {

    private final UserRepository userRepository;
    private final ShoppingCartRepository shoppingCartRepository;

    @Inject
    public UserService(UserRepository userRepository, ShoppingCartRepository shoppingCartRepository){
        this.userRepository = userRepository;
        this.shoppingCartRepository = shoppingCartRepository;
    }

    @Override
    @Transactional
    public User register(User user){
        Optional<User> byUsername = userRepository.findByUsername(user.getUsername());
        if(byUsername.isPresent()){ throw new ConflictException("Username existiert bereits");}

        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        if(byEmail.isPresent()){ throw new ConflictException("Email existiert bereits");}

        try {
            User created = userRepository.save(user);
            shoppingCartRepository.save(new ShoppingCart(created.getUserId()));
            return created;
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Username oder Email existiert bereits");
            }
            throw e;
        }
    }

    @Override
    @Transactional
    public User update(User user){
        Optional<User> optionalUser = userRepository.findById(user.getUserId());

        if (optionalUser.isEmpty()){
            throw new NotFoundException("User mit ID: " + user.getUserId() + " nicht gefunden");
        }

        User exists = optionalUser.get();

        Optional<User> userWithSameUsername = userRepository.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getUserId().equals(user.getUserId())) {
            throw new ConflictException("Username bereits vergeben");
        }

        Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getUserId().equals(user.getUserId())) {
            throw new ConflictException("Email bereits vergeben");
        }

        exists.setUsername(user.getUsername());
        exists.setEmail(user.getEmail());
        exists.setPasswordHash(user.getPasswordHash());

        try {
            return userRepository.save(exists);
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
        } catch (PersistenceException e) {
            if (isConstraintViolation(e)) {
                throw new ConflictException("Username oder Email existiert bereits");
            }
            throw e;
        }
    }

    @Override
    public User findById(Long userId){
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){ throw new NotFoundException("User mit der ID: " + userId + "ist nicht vorhanden");}
        return user.get();
    }

    @Override
    public User findByEmail(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        return userOpt.orElse(null);
    }

    @Override
    public User findByUsername(String username) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        return userOpt.orElse(null);
    }

    @Override
    @Transactional
    public boolean delete(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }
        try {
            userRepository.delete(optionalUser.get());
            return true;
        } catch (OptimisticLockException e) {
            throw new ConcurrencyException("Concurrent modification detected");
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
