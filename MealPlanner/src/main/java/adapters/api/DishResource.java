package adapters.api;

import adapters.api.dto.DishResponse;
import adapters.api.mapper.ApiMapper;
import application.port.in.DishAPI;
import application.port.in.DishCreationCommand;
import domain.entity.Dish;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jakarta.persistence.OptimisticLockException;

import java.util.List;
import java.util.stream.Collectors;

@Path("/dishes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DishResource {

    @Inject
    DishAPI dishService;

    private static CacheControl dishCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    //ETAG Handling
    @Context
    Request req;

    @POST
    public Response createDish(@Valid DishRequest request, @Context UriInfo uriInfo) {
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

            DishResponse createdResponse = ApiMapper.toDishResponse(created);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", createdResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(created.getDishId()).toString());
            links.put("all", base.clone()
                    .path(DishResource.class)
                    .build().toString());
            addIngredientLinks(links, base);
            links.put("addIngredient", base.clone()
                    .path(DishResource.class)
                    .path("{dishId}/ingredients")
                    .build(created.getDishId()).toString());
            links.put("update", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(created.getDishId()).toString());
            links.put("delete", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(created.getDishId()).toString());
            response.put("_links", links);

            Response.ResponseBuilder builder = Response.created(
                            base.clone()
                                    .path(DishResource.class)
                                    .path("{id}")
                                    .build(created.getDishId()))
                    .entity(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @GET
    public Response getAllDishes(@QueryParam("page") Integer page,
                                 @QueryParam("size") Integer size,
                                 @Context UriInfo uriInfo) {
        UriBuilder base = uriInfo.getBaseUriBuilder();
        List<Dish> dishes = dishService.findAll();
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        if (pageNumber < 0 || pageSize <= 0) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "page muss >= 0 und size muss > 0 sein", uriInfo, uriInfo.getRequestUri().toString());
        }
        int total = dishes.size();
        int fromIndex = Math.min(pageNumber * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Dish> pageItems = dishes.subList(fromIndex, toIndex);

        List<java.util.Map<String, Object>> items = pageItems.stream()
                .map(d -> {
                    DishResponse dto = ApiMapper.toDishResponse(d);
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", dto);
                    java.util.Map<String, String> itemLinks = new java.util.HashMap<>();
                    itemLinks.put("self", base.clone()
                            .path(DishResource.class)
                            .path("{id}")
                            .build(d.getDishId()).toString());
                    itemLinks.put("update", base.clone()
                            .path(DishResource.class)
                            .path("{id}")
                            .build(d.getDishId()).toString());
                    itemLinks.put("delete", base.clone()
                            .path(DishResource.class)
                            .path("{id}")
                            .build(d.getDishId()).toString());
                    item.put("_links", itemLinks);
                    return item;
                })
                .collect(Collectors.toList());

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("items", items);
        response.put("page", pageNumber);
        response.put("size", pageSize);
        response.put("total", total);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", uriInfo.getRequestUriBuilder().build().toString());
        links.put("user", base.clone()
                .path(UserResource.class)
                .build()
                .toString());
        links.put("create", base.clone()
                .path(DishResource.class)
                .build().toString());
        addIngredientLinks(links, base);
        links.put("shoppingCarts", base.clone()
                .path(ShoppingCartResource.class)
                .build()
                .toString());
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

        // FÜR ETag
        EntityTag etag = ETagHelper.calculate(response);
        Response.ResponseBuilder builder = req.evaluatePreconditions(etag);
        CacheControl cacheControl = dishCacheControl();
        if (builder != null) {
            return builder.tag(etag).cacheControl(cacheControl).build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(response).tag(etag).cacheControl(cacheControl);
        Hypermedia.addLinkHeaders(responseBuilder, links);
        return responseBuilder.build();

    }

    @GET
    @Path("{id}")
    public Response getDishById(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        Dish dish;
        try {
            dish = dishService.findById(id);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
        if (dish == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "Dish nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        DishResponse dishResponse = ApiMapper.toDishResponse(dish);
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", dishResponse);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(DishResource.class)
                .path("{id}")
                .build(dish.getDishId()).toString());
        links.put("all", base.clone()
                .path(DishResource.class)
                .build().toString());
        addIngredientLinks(links, base);
        links.put("addIngredient", base.clone()
                .path(DishResource.class)
                .path("{dishId}/ingredients")
                .build(dish.getDishId()).toString());
        links.put("update", base.clone()
                .path(DishResource.class)
                .path("{id}")
                .build(dish.getDishId()).toString());
        links.put("delete", base.clone()
                .path(DishResource.class)
                .path("{id}")
                .build(dish.getDishId()).toString());
        response.put("_links", links);

        //ETag
        EntityTag etag = ETagHelper.calculate(dishResponse);
        Response.ResponseBuilder builder = req.evaluatePreconditions(etag);
        CacheControl cacheControl = dishCacheControl();
        if (builder != null) {
            return builder.tag(etag).cacheControl(cacheControl).build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(response).tag(etag).cacheControl(cacheControl);
        Hypermedia.addLinkHeaders(responseBuilder, links);
        return responseBuilder.build();
    }

    @PUT
    @Path("{id}")
    public Response updateDish(@PathParam("id") Long id,
                               @Valid DishRequest request,
                               @Context UriInfo uriInfo,
                               @HeaderParam("If-Match") String ifMatch) {

        Dish currentDish = dishService.findById(id);
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        DishResponse currentResponse = ApiMapper.toDishResponse(currentDish);
        EntityTag currentTag = ETagHelper.calculate(currentResponse);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        try {
            Dish updated = dishService.update(
                    id,
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

            DishResponse updatedResponse = ApiMapper.toDishResponse(updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            links.put("all", base.clone()
                    .path(DishResource.class)
                    .build().toString());
            addIngredientLinks(links, base);
            links.put("addIngredient", base.clone()
                    .path(DishResource.class)
                    .path("{dishId}/ingredients")
                    .build(updated.getDishId()).toString());
            links.put("update", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            links.put("delete", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            response.put("_links", links);

            EntityTag newEtag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newEtag)
                    .cacheControl(dishCacheControl());
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (OptimisticLockException e) {
            return Hypermedia.error(Response.Status.CONFLICT, "Concurrent modification detected. Please retry.", uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteDish(@PathParam("id") Long id,
                               @Context UriInfo uriInfo,
                               @HeaderParam("If-Match") String ifMatch) {
        Dish dish;
        try {
            dish = dishService.findById(id);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        DishResponse currentResponse = ApiMapper.toDishResponse(dish);
        EntityTag currentTag = ETagHelper.calculate(currentResponse);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        try {
            dishService.delete(id);
        } catch (OptimisticLockException e) {
            return Hypermedia.error(Response.Status.CONFLICT, "Concurrent modification detected. Please retry.", uriInfo, uriInfo.getRequestUri().toString());
        }

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", "deleted");
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("all", base.clone()
                .path(DishResource.class)
                .build().toString());
        links.put("create", base.clone()
                .path(DishResource.class)
                .build().toString());
        addIngredientLinks(links, base);
        response.put("_links", links);

        Response.ResponseBuilder responseBuilder = Response.ok(response)
                .cacheControl(dishCacheControl());
        Hypermedia.addLinkHeaders(responseBuilder, links);
        return responseBuilder.build();
    }

    @POST
    @Path("{dishId}/ingredients")
    public Response addIngredient(@PathParam("dishId") Long dishId,
                                  @Valid DishIngredientRequest request,
                                  @Context UriInfo uriInfo) {
        try {
            Dish updated = dishService.addIngredient(dishId, request.foodItemId(), request.weight());
            DishResponse updatedResponse = ApiMapper.toDishResponse(updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            addIngredientLinks(links, base);
            links.put("updateIngredient", base.clone()
                    .path(DishResource.class)
                    .path("{dishId}/ingredients/{foodItemId}")
                    .build(updated.getDishId(), request.foodItemId()).toString());
            links.put("update", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            links.put("delete", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            response.put("_links", links);

            EntityTag newEtag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newEtag)
                    .cacheControl(dishCacheControl());
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (OptimisticLockException e) {
            return Hypermedia.error(Response.Status.CONFLICT, "Concurrent modification detected. Please retry.", uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @PATCH
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response updateIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId,
                                     @Valid DishIngredientWeightRequest request,
                                     @Context UriInfo uriInfo,
                                     @HeaderParam("If-Match") String ifMatch) {
        Dish currentDish = dishService.findById(dishId);
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        DishResponse currentResponse = ApiMapper.toDishResponse(currentDish);
        EntityTag currentTag = ETagHelper.calculate(currentResponse);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        try {
            Dish updated = dishService.updateIngredientWeight(dishId, foodItemId, request.weight());
            DishResponse updatedResponse = ApiMapper.toDishResponse(updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            addIngredientLinks(links, base);
            links.put("removeIngredient", base.clone()
                    .path(DishResource.class)
                    .path("{dishId}/ingredients/{foodItemId}")
                    .build(updated.getDishId(), foodItemId).toString());
            links.put("update", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            links.put("delete", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            response.put("_links", links);

            EntityTag newEtag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newEtag)
                    .cacheControl(dishCacheControl());
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (OptimisticLockException e) {
            return Hypermedia.error(Response.Status.CONFLICT, "Concurrent modification detected. Please retry.", uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @DELETE
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response removeIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId,
                                     @Context UriInfo uriInfo,
                                     @HeaderParam("If-Match") String ifMatch) { // <<< neu: If-Match hinzugefügt
        Dish currentDish = dishService.findById(dishId); // <<< neu
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        DishResponse currentResponse = ApiMapper.toDishResponse(currentDish);
        EntityTag currentTag = ETagHelper.calculate(currentResponse);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        try {
            Dish updated = dishService.removeIngredient(dishId, foodItemId);
            DishResponse updatedResponse = ApiMapper.toDishResponse(updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            addIngredientLinks(links, base);
            links.put("addIngredient", base.clone()
                    .path(DishResource.class)
                    .path("{dishId}/ingredients")
                    .build(updated.getDishId()).toString());
            links.put("update", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            links.put("delete", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            response.put("_links", links);

            EntityTag newEtag = ETagHelper.calculate(updatedResponse); // <<< neu
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newEtag)
                    .cacheControl(dishCacheControl()); // <<< neu
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (OptimisticLockException e) {
            return Hypermedia.error(Response.Status.CONFLICT, "Concurrent modification detected. Please retry.", uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    private void addIngredientLinks(java.util.Map<String, String> links, UriBuilder base) {
        links.put("foodItems", base.clone()
                .path(FoodItemResource.class)
                .build()
                .toString());
        links.put("foodItemsFilter", base.clone()
                .path(FoodItemResource.class)
                .queryParam("minProtein", 10)
                .queryParam("maxFat", 10)
                .queryParam("sortBy", "pricePerProtein")
                .queryParam("page", 0)
                .queryParam("size", 10)
                .build()
                .toString());
    }

}
