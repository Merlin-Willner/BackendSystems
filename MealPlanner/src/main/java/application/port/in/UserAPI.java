package application.port.in;

import domain.entity.User;

public interface UserAPI {
    User register(User user);
    User update(User user);
    User findById(Long userId);
    User findByEmail(String email);
    User findByUsername(String username);
    boolean delete(Long userId);
}
