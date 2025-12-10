package adapters.API;


import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


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
            ShoppingCart updated = shoppingCartService.addDishToCart(cartId, request.dishId(), multiplier);

            return Response.ok(updated).build();

        } catch (NotFoundException e){ // 404
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(e.getMessage())
                    .build();

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

}
