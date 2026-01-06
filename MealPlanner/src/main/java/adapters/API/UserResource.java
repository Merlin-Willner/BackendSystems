package adapters.API;

import application.port.in.UserAPI;
import domain.entity.User;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
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

    @POST
    public Response registerUser(@Valid UserRequest request, @Context UriInfo uriInfo){

        User user = new User( request.username(), request.email(), request.password());

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
        response.put("_links", links);

        return Response.created(
                        base.clone()
                                .path(UserResource.class)
                                .path("{id}")
                                .build(created.getUserId()))
                .entity(response)
                .build();

    }

    @GET
    @Path("{id}")
    public Response getUserById(@PathParam("id") Long id, @Context UriInfo uriInfo){
        User user = userService.findById(id);
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
        response.put("_links", links);
        return Response.ok(response).build();
    }

    @GET
    @Path("username/{username}")
    public Response getUserByUsername(@PathParam("username") String username, @Context UriInfo uriInfo) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User mit Username " + username + " nicht gefunden")
                    .build();
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
        response.put("_links", links);
        return Response.ok(response).build();
    }

    @GET
    @Path("email/{email}")
    public Response getUserByEmail(@PathParam("email") String email, @Context UriInfo uriInfo) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User mit Email " + email + " nicht gefunden")
                    .build();
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
        response.put("_links", links);
        return Response.ok(response).build();
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
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("page muss >= 0 und size muss > 0 sein")
                    .build();
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

        return Response.ok(response).build();
    }

    @PUT
    @Path("{id}")
    public Response updateUser(@PathParam("id") Long id, @Valid UserRequest request, @Context UriInfo uriInfo) {
        User userToUpdate = new User(request.username(), request.email(), request.password());
        userToUpdate.setUserId(id);
        User updatedUser = userService.update(userToUpdate);
        if (updatedUser == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("User konnte nicht aktualisiert werden (ID nicht gefunden oder Name/Email vergeben)")
                    .build();
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
        response.put("_links", links);
        return Response.ok(response).build();
    }



}
