package py.com.cds.framework.rest;

import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableMap;

import lombok.extern.log4j.Log4j2;

@Provider
@Log4j2
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

	@Override
	public Response toResponse(IllegalArgumentException exception) {

		return buildResponse(exception);
	}

	protected static Response buildResponse(IllegalArgumentException exception) {
		if (StringUtils.isEmpty(exception.getMessage()))
			log.debug("Illegal withotu message", exception);
		Map<String, Object> response = ImmutableMap.of("message", exception.getMessage(), "code", 400);
		return Response.status(400).entity(response).entity(response).build();
	}

}
