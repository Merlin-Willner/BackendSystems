package domain.service;

import application.port.out.UserRepository;
import domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository);
    }

    @Test
    @DisplayName("register rejects duplicate username")
    void registerRejectsDuplicateUsername() {
        User existing = new User("alice", "a@example.com", "pw");
        existing.setUserId(1L);

        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.register(new User("alice", "b@example.com", "pw")));
    }

    @DisplayName("register rejects duplicate email")
    void registerRejectsDuplicateEmail() {
        User existing = new User("alice", "a@example.com", "pw");
        existing.setUserId(1L);

        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.of(existing));

        assertThrows(IllegalArgumentException.class, () -> service.register(new User("bob", "a@example.com", "pw")));
    }

    @Test
    @DisplayName("register saves new user when unique")
    void registerSavesWhenUnique() {
        User newUser = new User("alice", "a@example.com", "pw");
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("a@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(newUser)).thenReturn(newUser);

        User saved = service.register(newUser);

        verify(userRepository).save(newUser);
        assertEquals(newUser, saved);
    }

    @Test
    @DisplayName("update returns null for missing user")
    void updateMissingUser() {
        User toUpdate = new User("alice", "a@example.com", "pw");
        toUpdate.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertNull(service.update(toUpdate));
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
        assertNull(service.update(conflictRequest)); // username conflict

        User emailConflictRequest = new User("unique", "o@example.com", "pw");
        emailConflictRequest.setUserId(1L);
        when(userRepository.findByUsername("unique")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("o@example.com")).thenReturn(Optional.of(otherUser));
        assertNull(service.update(emailConflictRequest)); // email conflict
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
    @DisplayName("findAll delegates to repository")
    void findAllDelegates() {
        when(userRepository.findAll()).thenReturn(List.of(new User("a", "a@x", "pw")));
        List<User> users = service.findAll();
        assertEquals(1, users.size());
    }
}
