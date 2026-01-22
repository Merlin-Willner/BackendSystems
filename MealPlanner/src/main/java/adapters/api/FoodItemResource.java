package adapters.api;

import application.port.in.FoodItemAPI;
import domain.entity.FoodItem;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.ws.rs.PathParam;


@Path("/food-items")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FoodItemResource {

    @Inject
    FoodItemAPI foodItemService;

    @Context
    Request req;

    private static CacheControl foodItemCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @POST
    public Response createFoodItem(@Valid FoodItemRequest request, @Context UriInfo uriInfo) {

        //Wen vorhanden dan fehler Conflict 409
        if(foodItemService.existsByName(request.name())) {
            return Hypermedia.error(Response.Status.CONFLICT,
                    "Ein FoodItem mit diesem Namen existiert bereits.",
                    uriInfo,
                    uriInfo.getRequestUri().toString());
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

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", created);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(FoodItemResource.class)
                    .path("{id}")
                    .build(created.getFoodItemId())
                    .toString());
            links.put("update", base.clone()
                    .path(FoodItemResource.class)
                    .path("{id}")
                    .build(created.getFoodItemId())
                    .toString());
            links.put("delete", base.clone()
                    .path(FoodItemResource.class)
                    .path("{id}")
                    .build(created.getFoodItemId())
                    .toString());
            addCollectionLinks(links, base);
            response.put("_links", links);

        Response.ResponseBuilder builder = Response.created(
                        UriBuilder.fromResource(FoodItemResource.class)
                                .path("{id}")
                                .build(created.getFoodItemId()))
                .entity(response);
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    } catch(IllegalArgumentException  e){
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
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
                                    @QueryParam("size") Integer size,
                                    @Context UriInfo uriInfo) {
        try {
            return buildFoodItemListResponse(minProtein, maxProtein, minCalories, maxCalories, minFat, maxFat, sortBy, page, size, uriInfo);
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @GET
    @Path("{id}")
    public Response getFoodItemById(@PathParam("id") Long id, @Context UriInfo uriInfo) {
        FoodItem item = foodItemService.findById(id);
        if (item == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "FoodItem nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", item);
        UriBuilder base = uriInfo.getBaseUriBuilder();
        Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(FoodItemResource.class)
                .path("{id}")
                .build(item.getFoodItemId())
                .toString());
        links.put("update", base.clone()
                .path(FoodItemResource.class)
                .path("{id}")
                .build(item.getFoodItemId())
                .toString());
        links.put("delete", base.clone()
                .path(FoodItemResource.class)
                .path("{id}")
                .build(item.getFoodItemId())
                .toString());
        addCollectionLinks(links, base);
        response.put("_links", links);
        EntityTag etag = ETagHelper.calculate(item);
        CacheControl cacheControl = foodItemCacheControl();
        Response.ResponseBuilder builder = req.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.tag(etag).cacheControl(cacheControl).build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(response)
                .tag(etag)
                .cacheControl(cacheControl);
        Hypermedia.addLinkHeaders(responseBuilder, links);
        return responseBuilder.build();
    }


    private Response buildFoodItemListResponse(Double minProtein,
                                               Double maxProtein,
                                               Double minCalories,
                                               Double maxCalories,
                                               Double minFat,
                                               Double maxFat,
                                               String sortBy,
                                               Integer page,
                                               Integer size,
                                               UriInfo uriInfo) {
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

        UriBuilder base = uriInfo.getBaseUriBuilder();
        List<Map<String, Object>> resultModels = pageItems.stream()
                .map(f -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", f);
                    Map<String, String> itemLinks = new java.util.HashMap<>();
                    itemLinks.put("self", base.clone()
                            .path(FoodItemResource.class)
                            .path("{id}")
                            .build(f.getFoodItemId())
                            .toString());
                    itemLinks.put("update", base.clone()
                            .path(FoodItemResource.class)
                            .path("{id}")
                            .build(f.getFoodItemId())
                            .toString());
                    itemLinks.put("delete", base.clone()
                            .path(FoodItemResource.class)
                            .path("{id}")
                            .build(f.getFoodItemId())
                            .toString());
                    item.put("_links", itemLinks);
                    return item;
                }).collect(Collectors.toList());

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("items", resultModels);
        response.put("page", pageNumber);
        response.put("size", pageSize);
        response.put("total", total);
        Map<String, String> links = new java.util.HashMap<>();
        links.put("self", uriInfo.getRequestUriBuilder().build().toString());
        addCollectionLinks(links, base);
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

        EntityTag etag = ETagHelper.calculate(response);
        CacheControl cacheControl = foodItemCacheControl();
        Response.ResponseBuilder builder = req.evaluatePreconditions(etag);
        if (builder != null) {
            return builder.tag(etag).cacheControl(cacheControl).build();
        }
        Response.ResponseBuilder responseBuilder = Response.ok(response)
                .tag(etag)
                .cacheControl(cacheControl);
        Hypermedia.addLinkHeaders(responseBuilder, links);
        return responseBuilder.build();
    }

    @PUT
    @Path("{id}")
    public Response updateFoodItem(@PathParam("id") Long id,
                                   @Valid FoodItemRequest request,
                                   @Context UriInfo uriInfo,
                                   @HeaderParam("If-Match") String ifMatch) {
        FoodItem current = foodItemService.findById(id);
        if (current == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "FoodItem nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        EntityTag currentTag = ETagHelper.calculate(current);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }
        try {
            FoodItem updated = foodItemService.update(id, new FoodItem(
                    request.name(),
                    request.brand(),
                    request.packSize(),
                    request.packPrice(),
                    request.proteinPer100g(),
                    request.carbsPer100g(),
                    request.fatPer100g(),
                    request.caloriesPer100g()
            ));

            if (updated == null) {
                return Hypermedia.error(Response.Status.NOT_FOUND, "FoodItem nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
            }

            Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(FoodItemResource.class)
                    .path("{id}")
                    .build(updated.getFoodItemId())
                    .toString());
            links.put("update", base.clone()
                    .path(FoodItemResource.class)
                    .path("{id}")
                    .build(updated.getFoodItemId())
                    .toString());
            links.put("delete", base.clone()
                    .path(FoodItemResource.class)
                    .path("{id}")
                    .build(updated.getFoodItemId())
                    .toString());
            addCollectionLinks(links, base);
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updated);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newTag)
                    .cacheControl(foodItemCacheControl());
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (IllegalArgumentException e) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @DELETE
    @Path("{id}")
    public Response deleteFoodItem(@PathParam("id") Long id,
                                   @Context UriInfo uriInfo,
                                   @HeaderParam("If-Match") String ifMatch) {
        FoodItem current = foodItemService.findById(id);
        if (current == null) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "FoodItem nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (ifMatch == null || ifMatch.isBlank()) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        EntityTag currentTag = ETagHelper.calculate(current);
        Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
        if (preconditions != null) {
            return preconditions.build();
        }

        boolean deleted;
        try {
            deleted = foodItemService.delete(id);
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
        if (!deleted) {
            return Hypermedia.error(Response.Status.NOT_FOUND, "FoodItem nicht gefunden", uriInfo, uriInfo.getRequestUri().toString());
        }

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", "deleted");
        UriBuilder base = uriInfo.getBaseUriBuilder();
        Map<String, String> links = new java.util.HashMap<>();
        addCollectionLinks(links, base);
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .cacheControl(foodItemCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    private void addCollectionLinks(Map<String, String> links, UriBuilder base) {
        links.put("all", base.clone()
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
        links.put("user", base.clone()
                .path(UserResource.class)
                .build()
                .toString());
        links.put("dishes", base.clone()
                .path(DishResource.class)
                .build()
                .toString());
        links.put("shoppingCarts", base.clone()
                .path(ShoppingCartResource.class)
                .build()
                .toString());
        links.put("create", base.clone()
                .path(FoodItemResource.class)
                .build()
                .toString());
    }
}
