package adapters.API;

import application.port.in.UserAPI;
import domain.entity.User;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserAPI userService;

    @Context
    Request req;

    private static CacheControl userCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @POST
    public Response registerUser(@Valid UserRequest request, @Context UriInfo uriInfo){

        try {
            User user = new User(request.username(), request.email(), request.password());
            User created = userService.register(user);

            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", created);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(UserResource.class)
                    .path("{id}")
                    .build(created.getUserId()).toString());
            links.put("all", base.clone()
                    .path(UserResource.class)
                    .build().toString());
            links.put("update", base.clone()
                    .path(UserResource.class)
                    .path("{id}")
                    .build(created.getUserId()).toString());
            links.put("delete", base.clone()
                    .path(UserResource.class)
                    .path("{id}")
                    .build(created.getUserId()).toString());
            response.put("_links", links);

            Response.ResponseBuilder builder = Response.created(
                            base.clone()
                                    .path(UserResource.class)
                                    .path("{id}")
                                    .build(created.getUserId()))
                    .entity(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.CONFLICT, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }

    }

    @GET
    @Path("{id}")
    public Response getUserById(@PathParam("id") Long id, @Context UriInfo uriInfo){
        User user;
        try {
            user = userService.findById(id);
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
                .path("{id}")
                .build(user.getUserId()).toString());
        links.put("all", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("update", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
        links.put("delete", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
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

    @GET
    @Path("username/{username}")
    public Response getUserByUsername(@PathParam("username") String username, @Context UriInfo uriInfo) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User mit Username " + username + " nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", user);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
        links.put("all", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("update", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
        links.put("delete", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
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

    @GET
    @Path("email/{email}")
    public Response getUserByEmail(@PathParam("email") String email, @Context UriInfo uriInfo) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User mit Email " + email + " nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", user);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
        links.put("all", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("update", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
        links.put("delete", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(user.getUserId()).toString());
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

    @GET
    public Response getAllUser(@QueryParam("page") Integer page,
                               @QueryParam("size") Integer size,
                               @Context UriInfo uriInfo){
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.List<User> users = userService.findAll();
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        if (pageNumber < 0 || pageSize <= 0) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "page muss >= 0 und size muss > 0 sein", uriInfo, uriInfo.getRequestUri().toString());
        }
        int total = users.size();
        int fromIndex = Math.min(pageNumber * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        java.util.List<User> pageItems = users.subList(fromIndex, toIndex);

        java.util.List<java.util.Map<String, Object>> items = pageItems.stream()
                .map(u -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", u);
                    java.util.Map<String, String> itemLinks = new java.util.HashMap<>();
                    itemLinks.put("self", base.clone()
                            .path(UserResource.class)
                            .path("{id}")
                            .build(u.getUserId()).toString());
                    itemLinks.put("update", base.clone()
                            .path(UserResource.class)
                            .path("{id}")
                            .build(u.getUserId()).toString());
                    itemLinks.put("delete", base.clone()
                            .path(UserResource.class)
                            .path("{id}")
                            .build(u.getUserId()).toString());
                    item.put("_links", itemLinks);
                    return item;
                })
                .collect(java.util.stream.Collectors.toList());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("items", items);
        response.put("page", pageNumber);
        response.put("size", pageSize);
        response.put("total", total);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", uriInfo.getRequestUriBuilder().build().toString());
        links.put("create", base.clone()
                .path(UserResource.class)
                .build().toString());
        if ((pageNumber + 1) * pageSize < total) {
            links.put("next", uriInfo.getRequestUriBuilder()
                    .replaceQueryParam("page", pageNumber + 1)
                    .replaceQueryParam("size", pageSize)
                    .build()
                    .toString());
        }
        if (pageNumber > 0) {
            links.put("prev", uriInfo.getRequestUriBuilder()
                    .replaceQueryParam("page", pageNumber - 1)
                    .replaceQueryParam("size", pageSize)
                    .build()
                    .toString());
        }
        response.put("_links", links);

        EntityTag etag = ETagHelper.calculate(response);
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
    @Path("{id}")
    public Response updateUser(@PathParam("id") Long id,
                               @Valid UserRequest request,
                               @Context UriInfo uriInfo,
                               @HeaderParam("If-Match") String ifMatch) {
        User current;
        try {
            current = userService.findById(id);
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.CONFLICT, "User konnte nicht aktualisiert werden (ID nicht gefunden oder Name/Email vergeben)", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        EntityTag currentTag = ETagHelper.calculate(current);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        User userToUpdate = new User(request.username(), request.email(), request.password());
        userToUpdate.setUserId(id);
        User updatedUser = userService.update(userToUpdate);
        if (updatedUser == null) {
            return Hypermedia.error(Response.Status.CONFLICT, "User konnte nicht aktualisiert werden (ID nicht gefunden oder Name/Email vergeben)", uriInfo, uriInfo.getRequestUri().toString());
        }
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", updatedUser);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(updatedUser.getUserId()).toString());
        links.put("all", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("update", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(updatedUser.getUserId()).toString());
        links.put("delete", base.clone()
                .path(UserResource.class)
                .path("{id}")
                .build(updatedUser.getUserId()).toString());
        response.put("_links", links);
        EntityTag newTag = ETagHelper.calculate(updatedUser);
        Response.ResponseBuilder builder = Response.ok(response)
                .tag(newTag)
                .cacheControl(userCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteUser(@PathParam("id") Long id,
                               @Context UriInfo uriInfo,
                               @HeaderParam("If-Match") String ifMatch) {
        User current;
        try {
            current = userService.findById(id);
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        EntityTag currentTag = ETagHelper.calculate(current);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        boolean deleted = userService.delete(id);
        if (!deleted) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "User nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", "deleted");
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("all", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("create", base.clone()
                .path(UserResource.class)
                .build().toString());
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .cacheControl(userCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }



}
