package adapters.API;

import application.port.in.ShoppingCartAPI;
import domain.entity.CartItem;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cart-items")
@Produces(MediaType.APPLICATION_JSON)
public class CartItemResource {

    @Inject
    ShoppingCartAPI shoppingCartService;

    @GET
    @Path("{cartItemId}")
    public Response getCartItemById(@PathParam("cartItemId") Long cartItemId) {
        try {
            CartItem item = shoppingCartService.getCartItemById(cartItemId);
            return Response.ok(item).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(e.getMessage())
                    .build();
        }
    }
}
