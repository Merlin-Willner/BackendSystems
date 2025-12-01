package adapters.API;

import application.port.in.FoodItemAPI;
import domain.entity.FoodItem;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.util.List;

@Path("/food-items")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FoodItemResource {

    @Inject
    FoodItemAPI foodItemService;

    @POST
    public Response createFoodItem(@Valid FoodItemRequest request) {
        FoodItem foodItem = new FoodItem(
                request.name(),
                request.brand(),
                request.packSize(),
                request.packPrice(),
                request.proteinPer100g(),
                request.carbsPer100g(),
                request.fatPer100g(),
                request.caloriesPer100g()
        );

        FoodItem created = foodItemService.create(foodItem);

        return Response.created(
                        UriBuilder.fromResource(FoodItemResource.class)
                                .path("{id}")
                                .build(created.getFoodItemId()))
                .entity(created)
                .build();
    }

    @GET
    public List<FoodItem> getAllFoodItems() {
        return foodItemService.findAll();
    }
}
