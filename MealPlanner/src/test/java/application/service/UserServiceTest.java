package application.service;

import application.exception.ConflictException;
import application.exception.NotFoundException;
import application.port.out.ShoppingCartRepository;
import application.port.out.UserRepository;
import domain.entity.ShoppingCart;
import domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockMakers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock(mockMaker = MockMakers.SUBCLASS)
    UserRepository userRepository;

    @Mock(mockMaker = MockMakers.SUBCLASS)
    ShoppingCartRepository shoppingCartRepository;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, shoppingCartRepository);
    }

    @Test
    @DisplayName("register rejects duplicate username")
    void registerRejectsDuplicateUsername() {
        User existing = new User("alice", "a@example.com", "pw");
        existing.setUserId(1L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> service.register(new User("alice", "b@example.com", "pw")));
    }

    @Test
    @DisplayName("register rejects duplicate email")
    void registerRejectsDuplicateEmail() {
        User existing = new User("alice", "a@example.com", "pw");
        existing.setUserId(1L);

        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> service.register(new User("bob", "a@example.com", "pw")));
    }

    @Test
    @DisplayName("register saves new user when unique")
    void registerSavesWhenUnique() {
        User newUser = new User("alice", "a@example.com", "pw");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(shoppingCartRepository.save(any(ShoppingCart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User saved = service.register(newUser);

        verify(userRepository).save(newUser);
        verify(shoppingCartRepository).save(any(ShoppingCart.class));
        assertEquals(newUser, saved);
    }

    @Test
    @DisplayName("update throws NotFoundException for missing user")
    void updateMissingUser() {
        User toUpdate = new User("alice", "a@example.com", "pw");
        toUpdate.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.update(toUpdate));
    }

    @Test
    @DisplayName("update rejects conflicting username or email")
    void updateRejectsConflicts() {
        User existing = new User("existing", "e@example.com", "pw");
        existing.setUserId(1L);

        User conflictRequest = new User("other", "new@example.com", "pw");
        conflictRequest.setUserId(1L);

        User otherUser = new User("other", "o@example.com", "pw");
        otherUser.setUserId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("other")).thenReturn(Optional.of(otherUser));
        assertThrows(ConflictException.class, () -> service.update(conflictRequest)); // username conflict

        User emailConflictRequest = new User("unique", "o@example.com", "pw");
        emailConflictRequest.setUserId(1L);
        when(userRepository.findByUsername("unique")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("o@example.com")).thenReturn(Optional.of(otherUser));
        assertThrows(ConflictException.class, () -> service.update(emailConflictRequest)); // email conflict
    }

    @Test
    @DisplayName("update saves changes when no conflicts")
    void updateSuccess() {
        User existing = new User("alice", "a@example.com", "pw");
        existing.setUserId(1L);

        User updatedData = new User("newname", "new@example.com", "newpw");
        updatedData.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByUsername("newname")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(existing)).thenReturn(existing);

        User result = service.update(updatedData);

        verify(userRepository).save(existing);
        assertEquals("newname", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
        assertEquals("newpw", result.getPasswordHash());
    }

    @Test
    @DisplayName("delete removes existing user")
    void deleteRemovesUser() {
        User user = new User("alice", "a@example.com", "pw");
        user.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        boolean result = service.delete(1L);

        assertTrue(result);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("delete returns false for missing user")
    void deleteReturnsFalseForMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        boolean result = service.delete(999L);

        assertFalse(result);
    }

    @Test
    @DisplayName("findById returns user when exists")
    void findByIdReturnsUser() {
        User user = new User("alice", "a@example.com", "pw");
        user.setUserId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = service.findById(1L);

        assertEquals(user, result);
    }

    @Test
    @DisplayName("findById throws for missing user")
    void findByIdThrowsForMissing() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.findById(999L));
    }

    @Test
    @DisplayName("findByUsername returns user when exists")
    void findByUsernameReturnsUser() {
        User user = new User("alice", "a@example.com", "pw");

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        User result = service.findByUsername("alice");

        assertEquals(user, result);
    }

    @Test
    @DisplayName("findByUsername returns null when not found")
    void findByUsernameReturnsNullWhenNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        User result = service.findByUsername("unknown");

        assertNull(result);
    }

    @Test
    @DisplayName("findByEmail returns user when exists")
    void findByEmailReturnsUser() {
        User user = new User("alice", "a@example.com", "pw");

        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(user));

        User result = service.findByEmail("a@example.com");

        assertEquals(user, result);
    }

    @Test
    @DisplayName("findByEmail returns null when not found")
    void findByEmailReturnsNullWhenNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        User result = service.findByEmail("unknown@example.com");

        assertNull(result);
    }
}
