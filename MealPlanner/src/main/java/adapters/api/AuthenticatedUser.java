package adapters.api;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;

public final class AuthenticatedUser {

    private AuthenticatedUser() {
    }

    public static Long userId(SecurityContext securityContext) {
        if (securityContext == null) {
            return null;
        }
        Principal principal = securityContext.getUserPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserId();
        }
        return null;
    }
}
