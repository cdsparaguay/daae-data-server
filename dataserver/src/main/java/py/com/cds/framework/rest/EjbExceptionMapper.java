package py.com.cds.framework.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBException;
import javax.persistence.PersistenceException;
import javax.transaction.RollbackException;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.hibernate.TransientPropertyValueException;

import com.google.common.collect.ImmutableMap;

@Provider
public class EjbExceptionMapper implements ExceptionMapper<EJBException> {

	@Override
	public Response toResponse(EJBException exception) {

		Throwable cause = exception.getCause();
		if (cause instanceof NullPointerException)
			return NullPointerExceptionMapper.buildResponse((NullPointerException) cause);
		if (cause instanceof IllegalArgumentException)
			return IllegalArgumentExceptionMapper.buildResponse((IllegalArgumentException) cause);
		if (cause instanceof IllegalStateException)
			return IllegalStateExceptionMapper.buildResponse((IllegalStateException) cause);

		if (cause instanceof IllegalStateException)
			if (cause.getCause() instanceof TransientPropertyValueException)
				return buildTransientPropertyValueException((TransientPropertyValueException) cause.getCause());

		if (cause instanceof TransientPropertyValueException) {
			return buildTransientPropertyValueException((TransientPropertyValueException) cause);
		}
		if (cause instanceof WebApplicationException) {
			return ((WebApplicationException) cause).getResponse();
		}

		if (cause instanceof RollbackException) {
			RollbackException rbe = (RollbackException) cause;
			if (rbe.getCause() instanceof IllegalStateException) {
				IllegalStateException ise = (IllegalStateException) rbe.getCause();
				if (ise.getCause() instanceof TransientPropertyValueException) {
					return buildTransientPropertyValueException((TransientPropertyValueException) ise.getCause());
				}
			}
			if (rbe.getCause() instanceof PersistenceException) {
				PersistenceException pe = (PersistenceException) rbe.getCause();
				if (pe.getCause() instanceof ConstraintViolationException) {
					return ValidationExceptionMapper.buildResponse((ConstraintViolationException) pe.getCause(), null);
				}
				if (pe.getCause() instanceof org.hibernate.exception.ConstraintViolationException) {
					return buildHibernateConstraintViolationException(
							(org.hibernate.exception.ConstraintViolationException) pe.getCause());
				}
			}
			if (rbe.getCause() instanceof ConstraintViolationException) {
				return ValidationExceptionMapper.buildResponse((ConstraintViolationException) rbe.getCause(), null);
			}
		}
		String message = cause.getMessage() == null ? "" : cause.getMessage();

		return Response.serverError()
				.entity(ImmutableMap.of("error", "Error interno del servidor", "code", 500, "detail", message))
				.build();
	}

	private Response buildHibernateConstraintViolationException(
			org.hibernate.exception.ConstraintViolationException cause) {
		Map<String, Object> response = new HashMap<>();
		response.put("error", "Entidad duplicada");
		response.put("code", 400);
		response.put("detail", String.format("La revision de %s fallo", cause.getConstraintName()));
		return Response.status(Status.BAD_REQUEST).entity(response).build();
	}

	private Response buildTransientPropertyValueException(TransientPropertyValueException cause) {

		return Response.status(Status.BAD_REQUEST)
				.entity(ImmutableMap.of("error",
						"Relacion no encontrada",
						"code",
						400,
						"detail",
						"La/El " + cause.getPropertyName() + " No fue encontrado"))
				.build();
	}

}
