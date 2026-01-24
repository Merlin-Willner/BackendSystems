package adapters.api.resource;

import adapters.api.dto.ShoppingCartSummaryResponse;
import adapters.api.mapper.ApiMapper;
import adapters.api.security.AuthenticatedUser;
import adapters.api.security.UserAuthenticated;
import adapters.api.util.ETagHelper;
import adapters.api.util.Hypermedia;
import application.port.in.ShoppingCartSummary;
import application.port.in.ShoppingCartSummaryQuery;
import application.port.in.ShoppingCartAPI;
import domain.entity.ShoppingCart;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
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
@Produces(MediaType.APPLICATION_JSON)
public class ShoppingCartSummaryResource {

    @Inject
    ShoppingCartSummaryQuery cartService;

    @Inject
    ShoppingCartAPI shoppingCartService;

    @Context
    SecurityContext securityContext;

    @Context
    Request req;

    private static CacheControl cartSummaryCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @GET
    @Path("/summary")
    public Response getCartSummary(@Context UriInfo uriInfo) {
        Long authenticatedUserId = authenticatedUserId();
        if (authenticatedUserId == null) {
            return unauthorized(uriInfo);
        }
        try {
            ShoppingCart cart = shoppingCartService.getCartByUserId(authenticatedUserId);
            ShoppingCartSummary summary = cartService.getCartSummary(cart.getShoppingCartId());
            ShoppingCartSummaryResponse summaryResponse = ApiMapper.toSummaryResponse(summary);
            UriBuilder base = uriInfo.getBaseUriBuilder();
            java.util.Map<String, Object> response = new java.util.HashMap<>();
            response.put("data", summaryResponse);
            java.util.Map<String, String> links = new java.util.HashMap<>();
            links.put("self", base.clone()
                    .path(ShoppingCartSummaryResource.class)
                    .path("summary")
                    .build().toString());
            links.put("cart", base.clone()
                    .path(ShoppingCartResource.class)
                    .build().toString());
            links.put("addDish", base.clone()
                    .path(ShoppingCartResource.class)
                    .path("items")
                    .build().toString());
            links.put("foodItems", base.clone()
                    .path(FoodItemResource.class)
                    .build()
                    .toString());
            links.put("dishes", base.clone()
                    .path(DishResource.class)
                    .build()
                    .toString());
            response.put("_links", links);

            EntityTag etag = ETagHelper.calculate(summaryResponse);
            CacheControl cacheControl = cartSummaryCacheControl();
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

    private Long authenticatedUserId() {
        return AuthenticatedUser.userId(securityContext);
    }

    private Response unauthorized(UriInfo uriInfo) {
        return Hypermedia.error(Response.Status.UNAUTHORIZED, "Nicht authentifiziert", uriInfo, uriInfo.getRequestUri().toString());
    }
}
