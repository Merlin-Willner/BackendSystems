package adapters.API;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Provider
@UserAuthenticated
@Priority(Priorities.AUTHENTICATION)
public class UserAuthenticationFilter implements ContainerRequestFilter {

    private static final String BEARER_PREFIX = "bearer ";
    private static final String REALM = "Bearer realm=\"MealPlanner\"";

    @Inject
    JwtService jwtService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String header = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isBlank()) {
            abortUnauthorized(requestContext, "Authorization header fehlt");
            return;
        }

        String lowerHeader = header.toLowerCase(Locale.ROOT);
        if (!lowerHeader.startsWith(BEARER_PREFIX)) {
            abortUnauthorized(requestContext, "Authorization scheme nicht erlaubt");
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            abortUnauthorized(requestContext, "Authorization header ist ungültig");
            return;
        }

        UserPrincipal principal = jwtService.verifyToken(token);
        if (principal == null) {
            abortUnauthorized(requestContext, "Token ungültig oder abgelaufen");
            return;
        }

        SecurityContext existingContext = requestContext.getSecurityContext();
        boolean secure = existingContext != null && existingContext.isSecure();
        requestContext.setSecurityContext(new UserSecurityContext(principal, secure));
    }

    private void abortUnauthorized(ContainerRequestContext requestContext, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        Map<String, String> links = new HashMap<>();
        String selfHref = requestContext.getUriInfo().getRequestUri().toString();
        links.put("self", selfHref);
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.status(Response.Status.UNAUTHORIZED)
                .header(HttpHeaders.WWW_AUTHENTICATE, REALM)
                .entity(response);
        Hypermedia.addLinkHeaders(builder, links);
        requestContext.abortWith(builder.build());
    }
}
