package adapters.API;


import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
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
import jakarta.ws.rs.core.SecurityContext;

 

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
    public Response getAllCarts(@QueryParam("page") Integer page,
                                @QueryParam("size") Integer size,
                                @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        java.util.List<ShoppingCart> carts = shoppingCartService.findAll().stream()
                .filter(cart -> authenticatedUserId.equals(cart.getUserId()))
                .toList();
        int pageNumber = page == null ? 0 : page;
        int pageSize = size == null ? 20 : size;
        if (pageNumber < 0 || pageSize <= 0) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "page muss >= 0 und size muss > 0 sein", uriInfo, uriInfo.getRequestUri().toString());
        }
        int total = carts.size();
        int fromIndex = Math.min(pageNumber * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);
        java.util.List<ShoppingCart> pageItems = carts.subList(fromIndex, toIndex);

        UriBuilder base = uriInfo.getBaseUriBuilder();
        java.util.List<java.util.Map<String, Object>> items = pageItems.stream()
                .map(c -> {
                    java.util.Map<String, Object> item = new java.util.HashMap<>();
                    item.put("data", c);
                    java.util.Map<String, String> itemLinks = new java.util.HashMap<>();
                    itemLinks.put("self", base.clone()
                            .path(ShoppingCartResource.class)
                            .path("{cartId}")
                            .build(c.getShoppingCartId()).toString());
                    itemLinks.put("summary", base.clone()
                            .path(ShoppingCartSummaryResource.class)
                            .path("{cartId}/summary")
                            .build(c.getShoppingCartId()).toString());
                    itemLinks.put("addDish", base.clone()
                            .path(ShoppingCartResource.class)
                            .path("{cartId}/items")
                            .build(c.getShoppingCartId()).toString());
                    itemLinks.put("update", base.clone()
                            .path(ShoppingCartResource.class)
                            .path("{cartId}")
                            .build(c.getShoppingCartId()).toString());
                    itemLinks.put("delete", base.clone()
                            .path(ShoppingCartResource.class)
                            .path("{cartId}")
                            .build(c.getShoppingCartId()).toString());
                    item.put("_links", itemLinks);
                    return item;
                })
                .toList();

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("items", items);
        response.put("page", pageNumber);
        response.put("size", pageSize);
        response.put("total", total);
        java.util.Map<String, String> links = new java.util.HashMap<>();
        links.put("self", uriInfo.getRequestUriBuilder().build().toString());
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
    }

    @GET
    @Path("/{cartId}")
    public Response getCartById(@PathParam("cartId") Long cartId, @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart cart = shoppingCartService.getCartById(cartId);
            if (!authenticatedUserId.equals(cart.getUserId())) {
                return forbidden(uriInfo);
            }
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", cart);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(cartId).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}/items")
                    .build(cartId).toString());
            links.put("update", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            links.put("delete", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            response.put("_links", links);

            EntityTag etag = ETagHelper.calculate(cart);
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

    @PUT
    @Path("/{cartId}")
    public Response updateCart(@PathParam("cartId") Long cartId,
                               @Valid ShoppingCartCreateRequest request,
                               @Context UriInfo uriInfo,
                               @HeaderParam("If-Match") String ifMatch) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        if (request == null || request.userId() == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "userId nicht gegeben", uriInfo, uriInfo.getRequestUri().toString());
        }
        if (!authenticatedUserId.equals(request.userId())) {
            return forbidden(uriInfo);
        }

        try {
            ShoppingCart currentCart = shoppingCartService.getCartById(cartId);
            if (!authenticatedUserId.equals(currentCart.getUserId())) {
                return forbidden(uriInfo);
            }
            if (ifMatch == null || ifMatch.isBlank()) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            EntityTag currentTag = ETagHelper.calculate(currentCart);
            Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
            if (preconditions != null) {
                return preconditions.build();
            }
            ShoppingCart updated = shoppingCartService.updateCartUser(cartId, authenticatedUserId);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(cartId).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}/items")
                    .build(cartId).toString());
            links.put("update", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            links.put("delete", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updated);
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
    @Path("/{cartId}")
    public Response deleteCart(@PathParam("cartId") Long cartId,
                               @Context UriInfo uriInfo,
                               @HeaderParam("If-Match") String ifMatch) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart currentCart = shoppingCartService.getCartById(cartId);
            if (!authenticatedUserId.equals(currentCart.getUserId())) {
                return forbidden(uriInfo);
            }
            if (ifMatch == null || ifMatch.isBlank()) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            EntityTag currentTag = ETagHelper.calculate(currentCart);
            Response.ResponseBuilder preconditions = req.evaluatePreconditions(currentTag);
            if (preconditions != null) {
                return preconditions.build();
            }
            shoppingCartService.deleteCart(cartId);
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
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response)
                .cacheControl(cartCacheControl());
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }

    @POST
    @Path("/{cartId}/items")
    public Response addDishToCart(@PathParam("cartId") Long cartId,
                                  @Valid ShoppingCartRequest request,
                                  @Context UriInfo uriInfo){
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }

        if(request == null || request.dishId() == null){
            return Hypermedia.error(Response.Status.BAD_REQUEST, "dishId nicht gegeben", uriInfo, uriInfo.getRequestUri().toString());
        }


        // OPTIONAL: Servings Multiplier / Default = 1
        int multiplier;
        if (request.servingsMultiplier() == null){
            multiplier = 1;
        } else {
            multiplier = request.servingsMultiplier();
        }

        try {
            ShoppingCart currentCart = shoppingCartService.getCartById(cartId);
            if (!authenticatedUserId.equals(currentCart.getUserId())) {
                return forbidden(uriInfo);
            }
            ShoppingCart updated = shoppingCartService.addDishToCart(cartId, request.dishId(), multiplier);

            // HATEOAS-Links
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(cartId).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}/items")
                    .build(cartId).toString());
            links.put("update", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            links.put("delete", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(cartId).toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updated);
            Response.ResponseBuilder builder = Response.ok(response)
                    .tag(newTag)
                    .cacheControl(cartCacheControl());
            Hypermedia.addLinkHeaders(builder, links);
            return builder.build();

        } catch (IllegalArgumentException e) { // 400
            return Hypermedia.error(Response.Status.BAD_REQUEST, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());

        } catch (WebApplicationException e){ // 422
            Response.Status status = Response.Status.fromStatusCode(e.getResponse().getStatus());
            if (status == null) {
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
            return Hypermedia.error(status, e.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
        }
    }

    @POST
    @Path("/by-user/{userId}/items")
    public Response addDishToCartByUser(@PathParam("userId") Long userId,
                                        @Valid ShoppingCartRequest request,
                                        @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        if (!authenticatedUserId.equals(userId)) {
            return forbidden(uriInfo);
        }

        if (request == null || request.dishId() == null) {
            return Hypermedia.error(Response.Status.BAD_REQUEST, "dishId nicht gegeben", uriInfo, uriInfo.getRequestUri().toString());
        }

        int multiplier = (request.servingsMultiplier() == null) ? 1 : request.servingsMultiplier();

        try {

            ShoppingCart updated = shoppingCartService.addDishToCartByUser(userId, request.dishId(), multiplier);
            // HATEOAS-Links
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", updated);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("by-user/{userId}/items")
                    .build(userId).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("by-user/{userId}/items")
                    .build(userId).toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(updated.getShoppingCartId()).toString());
            links.put("update", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(updated.getShoppingCartId()).toString());
            links.put("delete", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(updated.getShoppingCartId()).toString());
            response.put("_links", links);

            EntityTag newTag = ETagHelper.calculate(updated);
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

    private Long authenticatedUserId() {
        return AuthenticatedUser.userId(securityContext);
    }

    private Response unauthorized(UriInfo uriInfo) {
        return Hypermedia.error(Response.Status.UNAUTHORIZED, "Nicht authentifiziert", uriInfo, uriInfo.getRequestUri().toString());
    }

    private Response forbidden(UriInfo uriInfo) {
        return Hypermedia.error(Response.Status.FORBIDDEN, "Zugriff verweigert", uriInfo, uriInfo.getRequestUri().toString());
    }

}
