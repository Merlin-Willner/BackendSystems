package adapters.API;

import application.port.in.UserAPI;
import domain.entity.User;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import java.util.List;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    UserAPI userService;


    @POST
    public Response registerUser(@Valid UserRequest request){

        User user = new User( request.username(), request.email(), request.password());

        User created = userService.register(user);

        return Response.created(
                        UriBuilder.fromResource(UserResource.class)
                                .path("{id}")
                                .build(created.getUserId()))
                .entity(created)
                .build();

    }

    @GET
    @Path("{id}")
    public Response getUserById(@PathParam("id") Long id){
        User user = userService.findById(id);
        return Response.ok(user).build();
    }

    @GET
    @Path("username/{username}")
    public Response getUserByUsername(@PathParam("username") String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User mit Username " + username + " nicht gefunden")
                    .build();
        }
        return Response.ok(user).build();
    }

    @GET
    @Path("email/{email}")
    public Response getUserByEmail(@PathParam("email") String email) {
        User user = userService.findByEmail(email);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User mit Email " + email + " nicht gefunden")
                    .build();
        }
        return Response.ok(user).build();
    }

    @GET
    public List<User> getAllUser(){
        return userService.findAll();
    }

    @PUT
    @Path("{id}")
    public Response updateUser(@PathParam("id") Long id, @Valid UserRequest request) {
        User userToUpdate = new User(request.username(), request.email(), request.password());
        userToUpdate.setUserId(id);
        User updatedUser = userService.update(userToUpdate);
        if (updatedUser == null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("User konnte nicht aktualisiert werden (ID nicht gefunden oder Name/Email vergeben)")
                    .build();
        }
        return Response.ok(updatedUser).build();
    }



}
