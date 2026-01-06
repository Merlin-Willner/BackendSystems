package adapters.API;

import application.port.in.ShoppingCartSummary;
import application.port.in.ShoppingCartSummaryQuery;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;


@Path("/shopping-carts")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartSummaryResource {

    @Inject
    ShoppingCartSummaryQuery cartService;

    @GET
    @Path("{cartId}/summary")
    public Response getCartSummary(@PathParam("cartId") Long cartId, @Context UriInfo uriInfo) {
        try {
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
            Hypermedia.addDispatcherLink(links, uriInfo);
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
}
