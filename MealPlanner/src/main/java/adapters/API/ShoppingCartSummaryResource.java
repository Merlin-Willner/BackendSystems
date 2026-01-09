package adapters.API;

import application.port.in.ShoppingCartSummary;
import application.port.in.ShoppingCartSummaryQuery;
import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;


@UserAuthenticated
@Path("/shopping-carts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartSummaryResource {

    @Inject
    ShoppingCartSummaryQuery cartService;

    @Inject
    ShoppingCartAPI shoppingCartService;

    @Context
    SecurityContext securityContext;

    @GET
    @Path("{cartId}/summary")
    public Response getCartSummary(@PathParam("cartId") Long cartId, @Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart cart = shoppingCartService.getCartById(cartId);
            if (!authenticatedUserId.equals(cart.getUserId())) {
                return forbidden(uriInfo);
            }
            ShoppingCartSummary summary = cartService.getCartSummary(cartId);
            // HATEOAS-Links
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", summary);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("{cartId}/summary")
                    .build(summary.cartId()).toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("{cartId}/items/from-dish")
                    .build(summary.cartId()).toString());
            response.put("_links", links);

            Response.ResponseBuilder builder = Response.ok(response);
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
