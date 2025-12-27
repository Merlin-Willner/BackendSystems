package adapters.API;

import application.port.in.DishAPI;
import application.port.in.DishCreationCommand;
import domain.entity.Dish;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.DELETE;
import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.stream.Collectors;

@Path("/dishes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DishResource {

    @Inject
    DishAPI dishService;

    @POST
    public Response createDish(@Valid DishRequest request) {
        try {
            Dish created = dishService.create(
                    new DishCreationCommand(
                            request.name(),
                            request.category(),
                            request.servingWeight(),
                            request.preparationTime(),
                            request.imageUrl(),
                            request.userId(),
                            request.ingredients().stream()
                                    .map(i -> new DishCreationCommand.IngredientCommand(i.foodItemId(), i.weight()))
                                    .collect(Collectors.toList())
                    )
            );

            return Response.created(
                            UriBuilder.fromResource(DishResource.class)
                                    .path("{id}")
                                    .build(created.getDishId()))
                    .entity(created)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    public List<Dish> getAllDishes() {
        return dishService.findAll();
    }

    @GET
    @Path("{id}")
    public Response getDishById(@PathParam("id") Long id) {
        Dish dish = dishService.findById(id);
        return Response.ok(dish).build();
    }

    @POST
    @Path("{dishId}/ingredients")
    public Response addIngredient(@PathParam("dishId") Long dishId,
                                  @Valid DishRequest.DishIngredientAddRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Zutat darf nicht null sein")
                    .build();
        }
        try {
            Dish updated = dishService.addIngredient(dishId, request.foodItemId(), request.weight());
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Concurrent modification detected. Bitte erneut versuchen.")
                    .build();
        }
    }

    @PATCH
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response updateIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId,
                                     @Valid DishRequest.DishIngredientWeightRequest request) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Gewicht darf nicht null sein")
                    .build();
        }
        try {
            Dish updated = dishService.updateIngredientWeight(dishId, foodItemId, request.weight());
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Concurrent modification detected. Bitte erneut versuchen.")
                    .build();
        }
    }

    @DELETE
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response removeIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId) {
        try {
            Dish updated = dishService.removeIngredient(dishId, foodItemId);
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (OptimisticLockException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Concurrent modification detected. Bitte erneut versuchen.")
                    .build();
        }
    }
}
