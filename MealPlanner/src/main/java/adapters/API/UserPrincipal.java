package adapters.API;

import java.security.Principal;
import java.util.Objects;

public final class UserPrincipal implements Principal {

    private final Long userId;
    private final String username;

    public UserPrincipal(Long userId, String username) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.username = Objects.requireNonNull(username, "username");
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return username;
    }
}
