package adapters.API;

import application.port.in.FoodItemAPI;
import domain.dispatcher.AllowedAction;
import domain.dispatcher.DispatcherState;
import domain.entity.FoodItem;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import java.util.List;
import java.util.stream.Collectors;

import adapters.API.hateoas.ResourceModel;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.PathParam;


@Path("/food-items")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FoodItemResource {

    @Inject
    FoodItemAPI foodItemService;

    @Inject
    domain.dispatcher.Dispatcher dispatcher;

    @POST
    public Response createFoodItem(@Valid FoodItemRequest request) {

        if (!dispatcher.isActionAllowed(domain.dispatcher.AllowedAction.CREATE_FOOD)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Aktion CREATE_FOOD im aktuellen State nicht erlaubt")
                    .build();
        }

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

            //HATEOAS: Resource erstellen und Links hinzufügen
            ResourceModel<FoodItem> model = new ResourceModel<>(created);
            model.addLink(Link.fromUri("/food-items/" + created.getFoodItemId()).rel("self").build());
            model.addLink(Link.fromUri("/food-items").rel("all").build());

        return Response.created(
                        UriBuilder.fromResource(FoodItemResource.class)
                                .path("{id}")
                                .build(created.getFoodItemId()))
                .entity(model)
                .build();
    } catch(IllegalArgumentException  e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    //Nicht in Usecase 1 erfoderlich sonder funktion somit
    @GET
    public Response getAllFoodItems() {
        if (!dispatcher.isActionAllowed(domain.dispatcher.AllowedAction.GET_ALL_FOOD)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Aktion GET_ALL_FOOD im aktuellen State nicht erlaubt")
                    .build();
        }

        List<ResourceModel<FoodItem>> result = foodItemService.findAll().stream()
                .map(f -> {
                    ResourceModel<FoodItem> model = new ResourceModel<>(f);
                    model.addLink(Link.fromUri("/food-items/" + f.getFoodItemId()).rel("self").build());
                    return model;
                }).collect(Collectors.toList());

        return Response.ok(result).build();
    }

    @GET
    @Path("{id}")
    public Response getFoodItemById(@PathParam("id") Long id) {
        if (!dispatcher.isActionAllowed(AllowedAction.GET_SINGLE_FOOD)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Aktion GET_SINGLE_FOOD im aktuellen State nicht erlaubt")
                    .build();
        }

        FoodItem item = foodItemService.findById(id);
        if (item == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        // State ändern
        dispatcher.setCurrentState(DispatcherState.FOOD_SINGLE_SELECTED);

        ResourceModel<FoodItem> model = new ResourceModel<>(item);
        model.addLink(Link.fromUri("/food-items").rel("all").build());
        return Response.ok(model).build();
    }


    //Use-Case 03 Filter/Search
    @GET
    @Path("/search")
    public Response searchFoodItem(@QueryParam("minProtein") Double minProtein,
                                    @QueryParam("maxProtein") Double maxProtein,
                                    @QueryParam("minCalories") Double minCalories,
                                    @QueryParam("maxCalories") Double maxCalories,
                                    @QueryParam("minFat") Double minFat,
                                    @QueryParam("maxFat") Double maxFat,
                                    @QueryParam("sortBy") String  sortBy){

        if (!dispatcher.isActionAllowed(AllowedAction.SEARCH_FOOD)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Aktion SEARCH_FOOD im aktuellen State nicht erlaubt")
                    .build();
        }

        try{
            // Filter & Sortierung durchführen
            List<FoodItem> result = foodItemService.filterAndRank(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy);
            // ResourceModel erstellen und Links hinzufügen
            List<ResourceModel<FoodItem>> resultModels = result.stream()
                    .map(f -> {
                        ResourceModel<FoodItem> model = new ResourceModel<>(f);
                        model.addLink(Link.fromUri("/food-items/" + f.getFoodItemId()).rel("self").build());
                        return model;
                    }).collect(Collectors.toList());
            return Response.ok(resultModels).build();
        } catch (IllegalArgumentException  e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }
}
