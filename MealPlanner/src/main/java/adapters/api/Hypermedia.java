package adapters.api;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.HashMap;
import java.util.Map;

public final class Hypermedia {
    private static final String MEDIA_TYPE = "application/json";

    private Hypermedia() {
    }

    public static void addLinkHeader(Response.ResponseBuilder builder,
                                     String href,
                                     String rel) {
        String value = "<" + href + ">;rel=\"" + rel + "\";type=\"" + MEDIA_TYPE + "\"";
        builder.header("Link", value);
    }

    public static void addLinkHeaders(Response.ResponseBuilder builder,
                                      Map<String, String> links) {
        for (Map.Entry<String, String> entry : links.entrySet()) {
            addLinkHeader(builder, entry.getValue(), entry.getKey());
        }
    }

    public static Response error(Response.Status status, String message, UriInfo uriInfo, String selfHref) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", message);
        Map<String, String> links = new HashMap<>();
        if (selfHref != null) {
            links.put("self", selfHref);
        }
        response.put("_links", links);
        Response.ResponseBuilder builder = Response.status(status).entity(response);
        addLinkHeaders(builder, links);
        return builder.build();
    }
}
