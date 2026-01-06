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
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

import java.util.List;
import java.util.stream.Collectors;

@Path("/dishes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DishResource {

    @Inject
    DishAPI dishService;

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

            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", created);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(created.getDishId()).toString());
            links.put("all", base.clone()
                    .path(DishResource.class)
                    .build().toString());
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
            Hypermedia.addDispatcherLink(links, uriInfo);
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
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", d);
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
        links.put("create", base.clone()
                .path(DishResource.class)
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
        Hypermedia.addDispatcherLink(links, uriInfo);
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .header("Cache-Control", "max-age=60");
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
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
        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", dish);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(DishResource.class)
                .path("{id}")
                .build(dish.getDishId()).toString());
        links.put("all", base.clone()
                .path(DishResource.class)
                .build().toString());
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
        Hypermedia.addDispatcherLink(links, uriInfo);
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .header("Cache-Control", "max-age=60");
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    @PUT
    @Path("{id}")
    public Response updateDish(@PathParam("id") Long id,
                               @Valid DishRequest request,
                               @Context UriInfo uriInfo) {
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

            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
            links.put("all", base.clone()
                    .path(DishResource.class)
                    .build().toString());
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
            Hypermedia.addDispatcherLink(links, uriInfo);
            response.put("_links", links);

            Response.ResponseBuilder builder = Response.ok(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @jakarta.ws.rs.DELETE
    @Path("{id}")
    public Response deleteDish(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        try {
            dishService.delete(id);
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
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
        Hypermedia.addDispatcherLink(links, uriInfo);
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response);
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    @POST
    @Path("{dishId}/ingredients")
    public Response addIngredient(@PathParam("dishId") Long dishId,
                                  @Valid DishIngredientRequest request,
                                  @Context UriInfo uriInfo) {
        try {
            Dish updated = dishService.addIngredient(dishId, request.foodItemId(), request.weight());
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
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
            Hypermedia.addDispatcherLink(links, uriInfo);
            response.put("_links", links);
            Response.ResponseBuilder builder = Response.ok(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @PATCH
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response updateIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId,
                                     @Valid DishIngredientWeightRequest request,
                                     @Context UriInfo uriInfo) {
        try {
            Dish updated = dishService.updateIngredientWeight(dishId, foodItemId, request.weight());
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
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
            Hypermedia.addDispatcherLink(links, uriInfo);
            response.put("_links", links);
            Response.ResponseBuilder builder = Response.ok(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @jakarta.ws.rs.DELETE
    @Path("{dishId}/ingredients/{foodItemId}")
    public Response removeIngredient(@PathParam("dishId") Long dishId,
                                     @PathParam("foodItemId") Long foodItemId,
                                     @Context UriInfo uriInfo) {
        try {
            Dish updated = dishService.removeIngredient(dishId, foodItemId);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(DishResource.class)
                    .path("{id}")
                    .build(updated.getDishId()).toString());
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
            Hypermedia.addDispatcherLink(links, uriInfo);
            response.put("_links", links);
            Response.ResponseBuilder builder = Response.ok(response);
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (jakarta.ws.rs.NotFoundException e) {
            return Hypermedia.error(Response.Status.NOT_FOUND, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

}
