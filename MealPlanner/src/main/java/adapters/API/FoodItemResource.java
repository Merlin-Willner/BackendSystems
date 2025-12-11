package adapters.API;

import application.port.in.FoodItemAPI;
import domain.entity.FoodItem;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
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

        //Wen vorhanden dan fehler Conflict 409
        if(foodItemService.existsByName(request.name())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Ein FoodItem mit diesem Namen existiert bereits.")
                    .build();
        }

        try {
            FoodItem created = foodItemService.create(
                    new FoodItem(
                            request.name(),
                            request.brand(),
                            request.packSize(),
                            request.packPrice(),
                            request.proteinPer100g(),
                            request.carbsPer100g(),
                            request.fatPer100g(),
                            request.caloriesPer100g()
                    )
            );

        return Response.created(
                        UriBuilder.fromResource(FoodItemResource.class)
                                .path("{id}")
                                .build(created.getFoodItemId()))
                .entity(created)
                .build();
    } catch(IllegalArgumentException  e){
            //fehler 400
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    //Nicht in Usecase 1 erfoderlich sonder funktion somit
    @GET
    public List<FoodItem> getAllFoodItems() {
        return foodItemService.findAll();
    }

    //Use-Case 03 Filter/Search
    @GET
    @Path("/search")
    public Response searchFoodIteam(@QueryParam("minProtein") Double minProtein,
                                    @QueryParam("maxProtein") Double maxProtein,
                                    @QueryParam("minCalories") Double minCalories,
                                    @QueryParam("maxCalories") Double maxCalories,
                                    @QueryParam("minFat") Double minFat,
                                    @QueryParam("maxFat") Double maxFat,
                                    @QueryParam("sortBy") String  sortBy){
        try{
            List<FoodItem> result = foodItemService.filterAndRank(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy);
            return Response.ok(result).build();
        } catch (IllegalArgumentException  e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
