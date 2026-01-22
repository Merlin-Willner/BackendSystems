package adapters.api;

import adapters.api.dto.UserResponse;
import adapters.api.mapper.ApiMapper;
import application.port.in.UserAPI;
import domain.entity.User;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@Path("/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    UserAPI userService;

    @Inject
    JwtService jwtService;

    @POST
    @Path("/login")
    public Response login(@Valid LoginRequest request, @Context UriInfo uriInfo) {
        if (request == null || request.username() == null || request.password() == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "Username oder Passwort fehlt", uriInfo, uriInfo.getRequestUri().toString());
        }

        User user = userService.findByUsername(request.username());
        if (user == null || !request.password().equals(user.getPasswordHash())) {
            return Hypermedia.error(Response.Status.UNAUTHORIZED, "Benutzername oder Passwort falsch", uriInfo, uriInfo.getRequestUri().toString());
        }

        String token = jwtService.createToken(user);
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("token", token);
        response.put("tokenType", "Bearer");
        response.put("expiresInMinutes", jwtService.getExpirationMinutes());
        response.put("userId", user.getUserId());
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(AuthResource.class)
                .path("login")
                .build()
                .toString());
        links.put("user", base.clone()
                .path(UserResource.class)
                .build()
                .toString());
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response);
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    @POST
    @Path("/registration")
    public Response register(@Valid UserRequest request, @Context UriInfo uriInfo) {
        if (request == null || request.username() == null || request.email() == null || request.password() == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "Username, Email oder Passwort fehlt", uriInfo, uriInfo.getRequestUri().toString());
        }
        try {
            User user = new User(request.username(), request.email(), request.password());
            User created = userService.register(user);
            UserResponse responseUser = ApiMapper.toUserResponse(created);

            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", responseUser);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(AuthResource.class)
                    .path("registration")
                    .build()
                    .toString());
            links.put("login", base.clone()
                    .path(AuthResource.class)
                    .path("login")
                    .build()
                    .toString());
            links.put("user", base.clone()
                    .path(UserResource.class)
                    .build()
                    .toString());
            response.put("_links", links);

            Response.ResponseBuilder builder = Response.created(
                            base.clone()
                    .path(UserResource.class)
                    .build())
                    .entity(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.CONFLICT, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }
}
