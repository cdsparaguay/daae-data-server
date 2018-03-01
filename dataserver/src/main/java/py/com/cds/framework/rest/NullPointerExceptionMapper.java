package py.com.cds.framework.rest;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.ImmutableMap;

@Provider
public class NullPointerExceptionMapper implements ExceptionMapper<NullPointerException> {

	@Override
	public Response toResponse(NullPointerException exception) {
		return buildResponse(exception);
	}

	protected static Response buildResponse(NullPointerException exception) {
		if (exception.getMessage() == null || exception.getMessage().isEmpty())
			exception.printStackTrace();
		
		String message = exception.getMessage() == null ? "": exception.getMessage();
		Map<String, Object> response = ImmutableMap.of("message", message, "code", 400);
		return Response.status(400).entity(response).entity(response).build();
	}

}
