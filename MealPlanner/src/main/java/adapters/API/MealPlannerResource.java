package adapters.API;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

import java.util.HashMap;
import java.util.Map;

@Path("/mealplanner")
@Produces(MediaType.APPLICATION_JSON)
public class MealPlannerResource {

    @Context
    Request req;

    private static CacheControl entryCacheControl() {
        CacheControl cacheControl = new CacheControl();
        cacheControl.setPrivate(true);
        cacheControl.setMaxAge(60);
        cacheControl.setMustRevalidate(true);
        return cacheControl;
    }

    @GET
    public Response getEntryPoint(@Context UriInfo uriInfo) {
        UriBuilder base = uriInfo.getBaseUriBuilder();
        Map<String, Object> response = new HashMap<>();
        response.put("data", "mealplanner");

        Map<String, String> links = new HashMap<>();
        links.put("self", base.clone()
                .path(MealPlannerResource.class)
                .build()
                .toString());
        links.put("registration", base.clone()
                .path(AuthResource.class)
                .path("registration")
                .build()
                .toString());
        links.put("login", base.clone()
                .path(AuthResource.class)
                .path("login")
                .build()
                .toString());
        response.put("_links", links);

        EntityTag etag = ETagHelper.calculate(response);
        CacheControl cacheControl = entryCacheControl();
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
}
