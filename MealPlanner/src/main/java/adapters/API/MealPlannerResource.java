package adapters.API;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Context;

import java.util.HashMap;
import java.util.Map;

@Path("/mealplanner")
@Produces(MediaType.APPLICATION_JSON)
public class MealPlannerResource {

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
        links.put("foodItems", base.clone()
                .path(FoodItemResource.class)
                .build()
                .toString());
        links.put("foodSearch", base.clone()
                .path(FoodItemResource.class)
                .path("search")
                .build()
                .toString());
        links.put("createFood", base.clone()
                .path(FoodItemResource.class)
                .build()
                .toString());
        links.put("dishes", base.clone()
                .path(DishResource.class)
                .build()
                .toString());
        links.put("createDish", base.clone()
                .path(DishResource.class)
                .build()
                .toString());
        links.put("users", base.clone()
                .path(UserResource.class)
                .build()
                .toString());
        links.put("createUser", base.clone()
                .path(UserResource.class)
                .build()
                .toString());
        links.put("login", base.clone()
                .path(AuthResource.class)
                .path("login")
                .build()
                .toString());
        links.put("createCart", base.clone()
                .path(ShoppingCartResource.class)
                .build()
                .toString());
        links.put("carts", base.clone()
                .path(ShoppingCartResource.class)
                .build()
                .toString());
        response.put("_links", links);

        Response.ResponseBuilder builder = Response.ok(response);
        Hypermedia.addLinkHeaders(builder, links);
        return builder.build();
    }
}
