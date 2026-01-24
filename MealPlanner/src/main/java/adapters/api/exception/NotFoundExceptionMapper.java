package adapters.api.exception;

import adapters.api.util.Hypermedia;

import application.exception.NotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<NotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(NotFoundException exception) {
        return Hypermedia.error(Response.Status.NOT_FOUND, exception.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
    }
}
