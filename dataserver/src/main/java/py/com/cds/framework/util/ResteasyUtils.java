package py.com.cds.framework.util;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.Validate;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;

public class ResteasyUtils {

	/**
	 * Return the input stream of the parth with the name specified.
	 * 
	 * @return the inputStream, or null if this parameters existsx, but has no
	 *         data.
	 * @throws IllegalArgumentException
	 *             if the form exists, but there is not a attribute with the
	 *             name 'name'
	 */
	public static InputStream getPart(MultipartFormDataInput dataInput, String name) {

		Validate.notNull(dataInput, "Can't extrat data from null input");
		Validate.isTrue(!dataInput.getFormDataMap().isEmpty(), "Form without parameters, can't extract part");

		InputStream file = null;

		for (InputPart inputPart : dataInput.getFormDataMap().get(name)) {
			try {
				file = inputPart.getBody(InputStream.class, null);
			} catch (IOException e) {
			}
			break;
		}
		return file;
	}
}
