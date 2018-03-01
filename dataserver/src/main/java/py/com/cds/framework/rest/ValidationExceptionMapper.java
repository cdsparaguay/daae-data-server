package py.com.cds.framework.rest;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.constraints.NotNull;
import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import lombok.Value;

@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ValidationException> {

	@Context
	ResourceInfo info;

	@Override
	public Response toResponse(ValidationException exception) {
		return buildResponse(exception, info);
	}

	public static Response buildResponse(ValidationException exception, ResourceInfo info) {
		Map<String, Object> response = new HashMap<>();
		response.put("code", 400);
		response.put("error", "Errores de validacion");
		response.put("detail", unwrapException(exception, info));
		return Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(response).build();
	}

	protected static List<ViolationError> unwrapException(Throwable t, ResourceInfo info) {

		return doUnwrapException(new ArrayList<ViolationError>(), t, info);
	}

	private static List<ViolationError> doUnwrapException(List<ViolationError> s, Throwable t, ResourceInfo info) {
		if (t == null)
			return s;

		if (t instanceof ConstraintViolationException) {
			addMessages((ConstraintViolationException) t, s, info);
		} else {
			s.add(new ViolationError("UNKNOW", t.getMessage()));
		}
		if (t.getCause() != null && t != t.getCause()) {
			doUnwrapException(s, t.getCause(), info);
		}
		return s;
	}

	private static List<ViolationError> addMessages(ConstraintViolationException cve, List<ViolationError> l,
			ResourceInfo info) {

		Set<ConstraintViolation<?>> violations = cve.getConstraintViolations();
		for (ConstraintViolation<?> cv : violations) {
			l.add(toMessage(cv, info));
		}
		return l;
	}

	private static ViolationError toMessage(ConstraintViolation<?> cv, ResourceInfo info) {

		if (cv.getPropertyPath() != null && cv.getRootBean() != null && cv.getMessage() != null) {
			return new ViolationError(cv.getPropertyPath().toString(), cv.getMessage());
		}

		// si no tiene estos datos, quiere decir que viene de rest.

		// this return something like 'joinToTapp'
		String methodName = info.getResourceMethod().getName();

		// this return something like 'joinToTapp.arg0'
		String violatorName = cv.getPropertyPath().toString();
		String property = "UNKNOW";

		if (violatorName.contains(methodName)) {

			int index = getIndexOfArg(removePrefix(methodName, violatorName));
			property = findName(index, info);

		} else {
			// check what happends if the violator is not an a attribute
		}
		// Handle scpecial cases, like notnull
		if (cv.getConstraintDescriptor().getAnnotation() instanceof NotNull) {
			return new ViolationError(property, "Not found");
		}
		return new ViolationError(property, cv.getMessage());

	}

	private static String findName(int index, ResourceInfo info) {

		Annotation[] annotations = info.getResourceMethod().getParameterAnnotations()[index];

		for (Annotation a : annotations) {
			if (a.annotationType().equals(PathParam.class)) {
				return ((PathParam) a).value();
			}
			if (a.annotationType().equals(QueryParam.class)) {
				return ((QueryParam) a).value();
			}
			if (a.annotationType().equals(FormParam.class)) {
				return ((FormParam) a).value();
			}
		}

		return "UNKNOW";
	}

	/**
	 * Removes the #original attribute from a dotted notation
	 * <p>
	 * Example: removePrefix("joinToTapp", "joinToTapp.arg0") -> "arg0"
	 * </p>
	 * 
	 * @param prefix
	 *            the prefix to remove
	 * @param original
	 *            the full string in dotted notation
	 * @return the original without the prefix.
	 */
	private static String removePrefix(String prefix, String original) {
		return original.replace(prefix + ".", "");
	}

	/**
	 * Returns the index, from a reflection generated params name.
	 * 
	 * <p>
	 * Example: getIndexOfArg('arg3') -> 3
	 * </p>
	 * 
	 * @param reflectionName
	 *            the reflection generated name
	 * @return the index
	 */
	private static int getIndexOfArg(String reflectionName) {
		return Integer.parseInt(reflectionName.substring(3));
	}

	@Value
	public static final class ViolationError {
		String property;
		String message;
	}

}
