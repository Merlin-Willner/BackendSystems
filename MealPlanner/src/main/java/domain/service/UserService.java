package domain.service;


import application.port.in.UserAPI;
import application.port.out.ShoppingCartRepository;
import application.port.out.UserRepository;
import domain.entity.ShoppingCart;
import domain.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.List;
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
        if(byUsername.isPresent()){ throw new IllegalArgumentException("Username existiert bereits");}

        Optional<User> byEmail = userRepository.findByEmail(user.getEmail());
        if(byEmail.isPresent()){ throw new IllegalArgumentException("Email existiert bereits");}

        User created = userRepository.save(user);
        shoppingCartRepository.save(new ShoppingCart(created.getUserId()));
        return created;
    }

    @Override
    @Transactional
    public User update(User user){
        Optional<User> optionalUser = userRepository.findById(user.getUserId());

        if (optionalUser.isEmpty()){
            System.out.println("User mit ID: " + user.getUserId() + " nicht gefunden");
            return null;
        }

        User exists = optionalUser.get();

        Optional<User> userWithSameUsername = userRepository.findByUsername(user.getUsername());
        if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getUserId().equals(user.getUserId())) {
            System.out.println("Username bereits vergeben");
            return null;
        }

        Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());
        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getUserId().equals(user.getUserId())) {
            System.out.println("Email bereits vergeben");
            return null;
        }

        exists.setUsername(user.getUsername());
        exists.setEmail(user.getEmail());
        exists.setPasswordHash(user.getPasswordHash());

        return userRepository.save(exists);
    }

    @Override
    public User findById(Long userId){
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty()){ throw new IllegalArgumentException("User mit der ID: " + userId + "ist nicht vorhanden");}
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
    public List<User> findAll(){
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public boolean delete(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty()) {
            return false;
        }
        userRepository.delete(optionalUser.get());
        return true;
    }

}
