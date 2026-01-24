package adapters.api.exception;

import adapters.api.util.Hypermedia;

import application.exception.UnprocessableException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class UnprocessableExceptionMapper implements ExceptionMapper<UnprocessableException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(UnprocessableException exception) {
        Response.Status status = Response.Status.fromStatusCode(422);
        if (status == null) {
            status = Response.Status.BAD_REQUEST;
        }
        return Hypermedia.error(status, exception.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
    }
}
