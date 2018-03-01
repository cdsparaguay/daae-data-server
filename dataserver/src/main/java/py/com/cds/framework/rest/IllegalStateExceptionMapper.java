package py.com.cds.framework.rest;

import java.util.Collections;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {

	@Override
	public Response toResponse(IllegalStateException exception) {
		return buildResponse(exception);
	}

	public static Response buildResponse(IllegalStateException exception) {
		return Response.status(Response.Status.EXPECTATION_FAILED)
				.entity(Collections.singletonMap("error", exception.getMessage()))
				.build();
	}

}
