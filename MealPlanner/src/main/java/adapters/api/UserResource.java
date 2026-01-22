package adapters.api;

import application.port.in.UserAPI;
import domain.entity.User;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@UserAuthenticated
@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserAPI userService;

    @Context
    Request req;

    @Context
    SecurityContext securityContext;

    private static CacheControl userCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @GET
    public Response getCurrentUser(@Context UriInfo uriInfo) {
        Long userId = authenticatedUserId();
        if (userId == null) {
            return unauthorized(uriInfo);
        }
        User user;
        try {
            user = userService.findById(userId);
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
        if (user == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", user);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("update", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("delete", base.clone()
                .path(UserResource.class)
                .build().toString());
        addHubLinks(links, base);
        response.put("_links", links);

        EntityTag etag = ETagHelper.calculate(user);
        CacheControl cacheControl = userCacheControl();
        Response.ResponseBuilder builder = req.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.tag(etag).cacheControl(cacheControl).build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(response)
                .tag(etag)
                .cacheControl(cacheControl);
        Hypermedia.addLinkHeaders(responseBuilder, links);
        return responseBuilder.build();
    }

    @PUT
    public Response updateCurrentUser(@Valid UserRequest request,
                                      @Context UriInfo uriInfo,
                                      @HeaderParam("If-Match") String ifMatch) {
        Long userId = authenticatedUserId();
        if (userId == null) {
            return unauthorized(uriInfo);
        }
        User current;
        try {
            current = userService.findById(userId);
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        EntityTag currentTag = ETagHelper.calculate(current);
        if (!ETagHelper.matches(ifMatch, currentTag)) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }

        User updatedUser;
        try {
            User userToUpdate = new User(request.username(), request.email(), request.password());
            userToUpdate.setUserId(userId);
            updatedUser = userService.update(userToUpdate);
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
        if (updatedUser == null) {
            return Hypermedia.error(Response.Status.CONFLICT, "Username oder Email bereits vergeben", uriInfo, uriInfo.getRequestUri().toString());
        }

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", updatedUser);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("update", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("delete", base.clone()
                .path(UserResource.class)
                .build().toString());
        addHubLinks(links, base);
        response.put("_links", links);

        EntityTag newTag = ETagHelper.calculate(updatedUser);
        Response.ResponseBuilder builder = Response.ok(response)
                .tag(newTag)
                .cacheControl(userCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    @DELETE
    public Response deleteCurrentUser(@Context UriInfo uriInfo,
                                      @HeaderParam("If-Match") String ifMatch) {
        Long userId = authenticatedUserId();
        if (userId == null) {
            return unauthorized(uriInfo);
        }
        User current;
        try {
            current = userService.findById(userId);
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        EntityTag currentTag = ETagHelper.calculate(current);
        if (!ETagHelper.matches(ifMatch, currentTag)) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }

        boolean deleted;
        try {
            deleted = userService.delete(userId);
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
        if (!deleted) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", "deleted");
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("login", base.clone()
                .path(AuthResource.class)
                .path("login")
                .build().toString());
        links.put("registration", base.clone()
                .path(AuthResource.class)
                .path("registration")
                .build().toString());
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .cacheControl(userCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    private Long authenticatedUserId() {
        return AuthenticatedUser.userId(securityContext);
    }

    private Response unauthorized(UriInfo uriInfo) {
        return Hypermedia.error(Response.Status.UNAUTHORIZED, "Nicht authentifiziert", uriInfo, uriInfo.getRequestUri().toString());
    }

    private void addHubLinks(java.util.Map<String, String> links, UriBuilder base) {
        links.put("foodItems", base.clone()
                .path(FoodItemResource.class)
                .build()
                .toString());
        links.put("dishes", base.clone()
                .path(DishResource.class)
                .build()
                .toString());
        links.put("shoppingCarts", base.clone()
                .path(ShoppingCartResource.class)
                .build()
                .toString());
    }
}
