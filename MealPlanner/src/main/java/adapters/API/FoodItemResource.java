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
import java.util.Map;
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
    public Response getAllFoodItems(@QueryParam("minProtein") Double minProtein,
                                    @QueryParam("maxProtein") Double maxProtein,
                                    @QueryParam("minCalories") Double minCalories,
                                    @QueryParam("maxCalories") Double maxCalories,
                                    @QueryParam("minFat") Double minFat,
                                    @QueryParam("maxFat") Double maxFat,
                                    @QueryParam("sortBy") String sortBy,
                                    @QueryParam("page") Integer page,
                                    @QueryParam("size") Integer size) {
        if (!dispatcher.isActionAllowed(domain.dispatcher.AllowedAction.GET_ALL_FOOD)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Aktion GET_ALL_FOOD im aktuellen State nicht erlaubt")
                    .build();
        }

        try {
            return buildFoodItemListResponse(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy, page, size);
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
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
        return Response.ok(model)
                .header("Cache-Control", "max-age=60")
                .build();
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
                                    @QueryParam("sortBy") String  sortBy,
                                    @QueryParam("page") Integer page,
                                    @QueryParam("size") Integer size){

        if (!dispatcher.isActionAllowed(AllowedAction.SEARCH_FOOD)) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Aktion SEARCH_FOOD im aktuellen State nicht erlaubt")
                    .build();
        }

        try {
            return buildFoodItemListResponse(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy, page, size);
        } catch (IllegalArgumentException  e){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        }
    }

    private Response buildFoodItemListResponse(Double minProtein,
                                               Double maxProtein,
                                               Double minCalories,
                                               Double maxCalories,
                                               Double minFat,
                                               Double maxFat,
                                               String sortBy,
                                               Integer page,
                                               Integer size) {
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        if (pageNumber < 0 || pageSize <= 0) {
            throw new IllegalArgumentException("page muss >= 0 und size muss > 0 sein");
        }

        List<FoodItem> result = foodItemService.filterAndRank(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy);
        int total = result.size();
        int fromIndex = Math.min(pageNumber * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<FoodItem> pageItems = result.subList(fromIndex, toIndex);

        List<ResourceModel<FoodItem>> resultModels = pageItems.stream()
                .map(f -> {
                    ResourceModel<FoodItem> model = new ResourceModel<>(f);
                    model.addLink(Link.fromUri("/food-items/" + f.getFoodItemId()).rel("self").build());
                    return model;
                }).collect(Collectors.toList());

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("items", resultModels);
        response.put("page", pageNumber);
        response.put("size", pageSize);
        response.put("total", total);

        return Response.ok(response)
                .header("Cache-Control", "max-age=60")
                .build();
    }
}
