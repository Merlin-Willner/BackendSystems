package adapters.API;


import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

 

@Path("/shopping-carts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartResource {

    @Inject
    ShoppingCartAPI shoppingCartService;

    @POST
    public Response createCart(@Valid ShoppingCartCreateRequest request, @Context UriInfo uriInfo) {
        if (request == null || request.userId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("userId nicht gegeben")
                    .build();
        }

        try {
            ShoppingCart created = shoppingCartService.createCart(request.userId());
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", created);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}")
                    .build(created.getShoppingCartId()).toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(created.getShoppingCartId()).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}/items/from-dish")
                    .build(created.getShoppingCartId()).toString());
            response.put("_links", links);

            return Response.created(
                            base.clone()
                                    .path(ShoppingCartResource.class)
                                    .path("{cartId}")
                                    .build(created.getShoppingCartId()))
                    .entity(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/{cartId}")
    public Response getCartById(@PathParam("cartId") Long cartId, @Context UriInfo uriInfo) {
        try {
            ShoppingCart cart = shoppingCartService.getCartById(cartId);
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
                    .path("{cartId}/items/from-dish")
                    .build(cartId).toString());
            response.put("_links", links);

            return Response.ok(response)
                    .header("Cache-Control", "max-age=60")
                    .build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/{cartId}/items/from-dish")
    public Response addDishToCart(@PathParam("cartId") Long cartId,
                                  @Valid ShoppingCartRequest request,
                                  @Context UriInfo uriInfo){

        if(request == null || request.dishId() == null){
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("dishId nicht gegeben")
                    .build();
        }


        // OPTIONAL: Servings Multiplier / Default = 1
        int multiplier;
        if (request.servingsMultiplier() == null){
            multiplier = 1;
        } else {
            multiplier = request.servingsMultiplier();
        }

        try {
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
                    .path("{cartId}/items/from-dish")
                    .build(cartId).toString());
            response.put("_links", links);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) { // 400
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(e.getMessage())
                    .build();

        } catch (WebApplicationException e){ // 422
            return Response.status(e.getResponse().getStatus())
                    .entity(e.getMessage())
                    .build();
        }
    }

    @POST
    @Path("/by-user/{userId}/items/from-dish")
    public Response addDishToCartByUser(@PathParam("userId") Long userId,
                                        @Valid ShoppingCartRequest request,
                                        @Context UriInfo uriInfo) {

        if (request == null || request.dishId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("dishId nicht gegeben")
                    .build();
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
                    .path("by-user/{userId}/items/from-dish")
                    .build(userId).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("by-user/{userId}/items/from-dish")
                    .build(userId).toString());
            links.put("summary", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(updated.getShoppingCartId()).toString());
            response.put("_links", links);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        }
    }

}
