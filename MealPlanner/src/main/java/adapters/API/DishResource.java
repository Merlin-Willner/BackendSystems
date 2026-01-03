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
    public Response getAllDishes() {
        return Response.ok(dishService.findAll())
                .header("Cache-Control", "max-age=60")
                .build();
    }

    @GET
    @Path("{id}")
    public Response getDishById(@PathParam("id") Long id) {
        Dish dish = dishService.findById(id);
        return Response.ok(dish)
                .header("Cache-Control", "max-age=60")
                .build();
    }

    @POST
    @Path("{dishId}/ingredients")
    public Response addIngredient(@PathParam("dishId") Long dishId,
                                  @Valid DishIngredientRequest request) {
        try {
            Dish updated = dishService.addIngredient(dishId, request.foodItemId(), request.weight());
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @PATCH
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response updateIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId,
                                     @Valid DishIngredientWeightRequest request) {
        try {
            Dish updated = dishService.updateIngredientWeight(dishId, foodItemId, request.weight());
            return Response.ok(updated).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

    @jakarta.ws.rs.DELETE
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
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();
        }
    }

}
