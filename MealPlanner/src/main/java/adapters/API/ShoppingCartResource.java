package adapters.API;


import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/shopping-carts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartResource {

    @Inject
    ShoppingCartAPI shoppingCartService;

    @POST
    @Path("/{cartId}/items/from-dish")
    public Response addDishToCart(@PathParam("cartId") Long cartId,
                                  @Valid ShoppingCartRequest request){

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
            ShoppingCart updated = shoppingCartService.addDishToCartByUser(cartId, request.dishId(), multiplier);

            // HATEOAS-Links
            Map<String, Object> response = new HashMap<>();
            response.put("cart", updated);
            Map<String, String> links = new HashMap<>();
            links.put("self", "/shopping-carts/" + cartId);
            links.put("summary", "/shopping-carts/" + cartId + "/summary");
            links.put("addDish", "/shopping-carts/" + cartId + "/items/from-dish");
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
                                        @Valid ShoppingCartRequest request) {

        if (request == null || request.dishId() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("dishId nicht gegeben")
                    .build();
        }

        int multiplier = (request.servingsMultiplier() == null) ? 1 : request.servingsMultiplier();

        try {

            ShoppingCart updated = shoppingCartService.addDishToCart(userId, request.dishId(), multiplier);
            // HATEOAS-Links
            Map<String, Object> response = new HashMap<>();
            response.put("cart", updated);
            Map<String, String> links = new HashMap<>();
            links.put("self", "/shopping-carts/by-user/" + userId + "/items/from-dish");
            links.put("summary", "/shopping-carts/" + updated.getShoppingCartId() + "/summary");
            links.put("addDish", "/shopping-carts/by-user/" + userId + "/items/from-dish");
            response.put("_links", links);

            return Response.ok(response).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();

        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus()).entity(e.getMessage()).build();
        }
    }

}
