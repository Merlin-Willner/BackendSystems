package adapters.api;

import application.exception.ValidationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ValidationException exception) {
        return Hypermedia.error(Response.Status.BAD_REQUEST, exception.getMessage(), uriInfo, uriInfo.getRequestUri().toString());
    }
}
