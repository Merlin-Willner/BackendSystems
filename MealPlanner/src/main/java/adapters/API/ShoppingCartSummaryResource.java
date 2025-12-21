package adapters.API;

import application.port.in.ShoppingCartSummaryQuery;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

@Path("/shopping-carts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartSummaryResource {

    @Inject
    ShoppingCartSummaryQuery cartService;

    @GET
    @Path("{cartId}/summary")
    public Response getCartSummary(@PathParam("cartId") Long cartId) {
        try {
            ShoppingCart summary = cartService.getCartSummary(cartId);
            // HATEOAS-Links
            Map<String, Object> response = new HashMap<>();
            response.put("cart", summary);
            Map<String, String> links = new HashMap<>();
            links.put("self", "/shopping-carts/" + summary.getShoppingCartId() + "/summary");
            links.put("addDish", "/shopping-carts/" + summary.getShoppingCartId() + "/items/from-dish");
            links.put("byUserAddDish", "/shopping-carts/by-user/{userId}/items/from-dish"); // {userId} hier Platzhalter
            response.put("_links", links);

            return Response.ok(response).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(e.getMessage())
                    .build();
        }
    }
}