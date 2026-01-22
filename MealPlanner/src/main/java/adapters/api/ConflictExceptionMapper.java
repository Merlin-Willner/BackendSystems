package adapters.api;

import application.exception.ConflictException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ConflictExceptionMapper implements ExceptionMapper<ConflictException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ConflictException exception) {
        return Hypermedia.error(Response.Status.CONFLICT, exception.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
    }
}
