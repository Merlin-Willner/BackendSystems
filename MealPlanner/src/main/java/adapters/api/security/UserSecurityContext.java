package adapters.api.security;

import jakarta.ws.rs.core.SecurityContext;

import java.security.Principal;
import java.util.Objects;

public final class UserSecurityContext implements SecurityContext {

    private final UserPrincipal principal;
    private final boolean secure;

    public UserSecurityContext(UserPrincipal principal, boolean secure) {
        this.principal = Objects.requireNonNull(principal, "principal");
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return principal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }
}
