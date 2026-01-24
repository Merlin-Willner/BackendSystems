package application.usecase;

import application.port.in.UserAPI;
import application.port.out.ShoppingCartRepository;
import application.port.out.UserRepository;
import application.service.UserService;
import domain.entity.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserUseCase implements UserAPI {

    private final UserService service;

    @Inject
    public UserUseCase(UserRepository userRepository, ShoppingCartRepository shoppingCartRepository) {
        this.service = new UserService(userRepository, shoppingCartRepository);
    }

    @Override
    @Transactional
    public User register(User user) {
        return service.register(user);
    }

    @Override
    @Transactional
    public User update(User user) {
        return service.update(user);
    }

    @Override
    public User findById(Long userId) {
        return service.findById(userId);
    }

    @Override
    public User findByEmail(String email) {
        return service.findByEmail(email);
    }

    @Override
    public User findByUsername(String username) {
        return service.findByUsername(username);
    }

    @Override
    @Transactional
    public boolean delete(Long userId) {
        return service.delete(userId);
    }
}
