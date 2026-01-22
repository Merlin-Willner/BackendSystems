package adapters.api;

import application.exception.ConcurrencyException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConcurrencyExceptionMapper implements ExceptionMapper<ConcurrencyException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConcurrencyException exception) {
        return Hypermedia.error(Response.Status.CONFLICT, exception.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
    }
}
