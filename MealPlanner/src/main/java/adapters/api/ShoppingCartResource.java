package adapters.api;

import adapters.api.dto.ShoppingCartResponse;
import adapters.api.mapper.ApiMapper;
import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;

@UserAuthenticated
@Path("/shopping-carts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartResource {

    @Inject
    ShoppingCartAPI shoppingCartService;

    @Context
    SecurityContext securityContext;

    @Context
    Request req;

    private static CacheControl cartCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @GET
    public Response getCurrentCart(@Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart cart = shoppingCartService.getCartByUserId(authenticatedUserId);
            ShoppingCartResponse cartResponse = ApiMapper.toShoppingCartResponse(cart);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", cartResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            links.put("user", base.clone()
                    .path(UserResource.class)
                    .build().toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("addFoodItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/food-items")
                    .build().toString());
            links.put("updateItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("removeItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("foodItems", base.clone()
                    .path(FoodItemResource.class)
                    .build()
                    .toString());
            links.put("dishes", base.clone()
                    .path(DishResource.class)
                    .build()
                    .toString());
            links.put("clear", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            response.put("_links", links);

            EntityTag etag = ETagHelper.calculate(cartResponse);
            CacheControl cacheControl = cartCacheControl();
            Response.ResponseBuilder builder = req.evaluatePreconditions(etag);
            if (builder != null) {
                return builder.tag(etag).cacheControl(cacheControl).build();
            }
            Response.ResponseBuilder responseBuilder = Response.ok(response)
                    .tag(etag)
                    .cacheControl(cacheControl);
            Hypermedia.addLinkHeaders(responseBuilder, links);
            return responseBuilder.build();
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @POST
    @Path("/items")
    public Response addDishToCart(@Valid ShoppingCartRequest request,
                                  @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        if (request == null || request.dishId() == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "dishId nicht gegeben", uriInfo, uriInfo.getRequestUri().toString());
        }

        int multiplier = (request.servingsMultiplier() == null) ? 1 : request.servingsMultiplier();

        try {
            ShoppingCart updated = shoppingCartService.addDishToCartByUser(authenticatedUserId, request.dishId(), multiplier);
            ShoppingCartResponse updatedResponse = ApiMapper.toShoppingCartResponse(updated);

            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            links.put("user", base.clone()
                    .path(UserResource.class)
                    .build().toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("addFoodItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/food-items")
                    .build().toString());
            links.put("updateItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("removeItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("foodItems", base.clone()
                    .path(FoodItemResource.class)
                    .build()
                    .toString());
            links.put("dishes", base.clone()
                    .path(DishResource.class)
                    .build()
                    .toString());
            links.put("clear", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newTag)
                    .cacheControl(cartCacheControl());
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

    @POST
    @Path("/items/food-items")
    public Response addFoodItemToCart(@Valid ShoppingCartFoodItemRequest request,
                                      @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        if (request == null || request.foodItemId() == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "foodItemId nicht gegeben", uriInfo, uriInfo.getRequestUri().toString());
        }
        int quantity = (request.quantity() == null) ? 1 : request.quantity();
        if (quantity <= 0) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "quantity muss positiv sein", uriInfo, uriInfo.getRequestUri().toString());
        }
        try {
            ShoppingCart updated = shoppingCartService.addFoodItemToCartByUser(authenticatedUserId, request.foodItemId(), quantity);
            ShoppingCartResponse updatedResponse = ApiMapper.toShoppingCartResponse(updated);

            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            links.put("user", base.clone()
                    .path(UserResource.class)
                    .build().toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("addFoodItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/food-items")
                    .build().toString());
            links.put("updateItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("removeItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("foodItems", base.clone()
                    .path(FoodItemResource.class)
                    .build()
                    .toString());
            links.put("dishes", base.clone()
                    .path(DishResource.class)
                    .build()
                    .toString());
            links.put("clear", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newTag)
                    .cacheControl(cartCacheControl());
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

    @PATCH
    @Path("/items/{foodItemId}")
    public Response updateCartItem(@PathParam("foodItemId") Long foodItemId,
                                   @Valid ShoppingCartItemUpdateRequest request,
                                   @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        if (request == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "quantity nicht gegeben", uriInfo, uriInfo.getRequestUri().toString());
        }
        try {
            ShoppingCart updated = shoppingCartService.updateItemQuantity(authenticatedUserId, foodItemId, request.quantity());
            ShoppingCartResponse updatedResponse = ApiMapper.toShoppingCartResponse(updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            links.put("user", base.clone()
                    .path(UserResource.class)
                    .build().toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("addFoodItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/food-items")
                    .build().toString());
            links.put("updateItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("removeItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("foodItems", base.clone()
                    .path(FoodItemResource.class)
                    .build()
                    .toString());
            links.put("dishes", base.clone()
                    .path(DishResource.class)
                    .build()
                    .toString());
            links.put("clear", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newTag)
                    .cacheControl(cartCacheControl());
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
    @Path("/items/{foodItemId}")
    public Response removeCartItem(@PathParam("foodItemId") Long foodItemId,
                                   @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart updated = shoppingCartService.removeItem(authenticatedUserId, foodItemId);
            ShoppingCartResponse updatedResponse = ApiMapper.toShoppingCartResponse(updated);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updatedResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            links.put("user", base.clone()
                    .path(UserResource.class)
                    .build().toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("addFoodItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/food-items")
                    .build().toString());
            links.put("updateItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("removeItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                    .build("foodItemId")
                    .toString());
            links.put("foodItems", base.clone()
                    .path(FoodItemResource.class)
                    .build()
                    .toString());
            links.put("dishes", base.clone()
                    .path(DishResource.class)
                    .build()
                    .toString());
            links.put("clear", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updatedResponse);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newTag)
                    .cacheControl(cartCacheControl());
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @DELETE
    public Response clearCart(@Context UriInfo uriInfo,
                              @HeaderParam("If-Match") String ifMatch) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart currentCart = shoppingCartService.getCartByUserId(authenticatedUserId);
            ShoppingCartResponse currentResponse = ApiMapper.toShoppingCartResponse(currentCart);
            if (ifMatch == null || ifMatch.isBlank()) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            EntityTag currentTag = ETagHelper.calculate(currentResponse);
            if (!ETagHelper.matches(ifMatch, currentTag)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            shoppingCartService.deleteCart(currentCart.getShoppingCartId());
        } catch (WebApplicationException e) {
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("data", "cleared");
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", base.clone()
                .path(ShoppingCartResource.class)
                .build().toString());
        links.put("user", base.clone()
                .path(UserResource.class)
                .build().toString());
        links.put("summary", base.clone()
                .path(ShoppingCartSummaryResource.class)
                .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("addFoodItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/food-items")
                    .build().toString());
            links.put("updateItem", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items/{foodItemId}")
                .build("foodItemId")
                .toString());
        links.put("removeItem", base.clone()
                .path(ShoppingCartResource.class)
                .path("items/{foodItemId}")
                .build("foodItemId")
                .toString());
        links.put("foodItems", base.clone()
                .path(FoodItemResource.class)
                .build()
                .toString());
        links.put("dishes", base.clone()
                .path(DishResource.class)
                .build()
                .toString());
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .cacheControl(cartCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    private Long authenticatedUserId() {
        return AuthenticatedUser.userId(securityContext);
    }

    private Response unauthorized(UriInfo uriInfo) {
        return Hypermedia.error(Response.Status.UNAUTHORIZED, "Nicht authentifiziert", uriInfo, uriInfo.getRequestUri().toString());
    }
}
