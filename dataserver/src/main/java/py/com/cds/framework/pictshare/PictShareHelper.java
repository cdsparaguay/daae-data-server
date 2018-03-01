package py.com.cds.framework.pictshare;

import java.io.IOException;
import java.io.InputStream;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.lang3.Validate;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import py.com.cds.framework.util.Config;

@Stateless
public class PictShareHelper {

	@Inject
	Config config;

	ObjectMapper mapper = new ObjectMapper();

	static String template;

	/**
	 * Transform a hash name (as stored in pictShare) to a standard URL to use.
	 * 
	 * <p>
	 * The result of this method is intended to use with the directive in the
	 * angular part.
	 * </p>
	 * 
	 * @param name
	 *            the name of the image, as returned from
	 *            {@link #uploadAndGetName(InputStream, String)}
	 * @return the full URL to download the image, with a placeholder to custom
	 *         the size and effects of the image.
	 */
	public static String getFullUrl(String name) {

		if (template == null)
			template = Config.getStaticWithoutCDI().getString("pictshare.template");

		Validate.notBlank(name, "Can't build a full URL name from a empty String");
		return template.replace("HASH_NAME", name);
	}

	/**
	 * Carga una imagen y retorna su nombre para posterior referencias.
	 * 
	 * @param image
	 *            imagen a cargar
	 * @param format
	 *            formato de la imagen
	 * @return nombre de la imagen
	 */
	public String uploadAndGetName(InputStream image, String format) {

		Validate.notNull(image, "Can't upload an empty image");
		Validate.notBlank(format, "Can't upload with empty format");

		PictShareResponse response = uploadImage(image, format);

		switch (response.getStatus()) {
		case "OK":
			return response.getHash();
		case "ERR":
			throw new IllegalArgumentException("No se puede subir la imagen " + response.getReason());
		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * El servidor retorna lago así:
	 * 
	 * <code>
	 * <pre>
	 * {
	 *   "status": "OK",
	 *   "type": "png",
	 *   "hash": "4cace5c54f.png",
	 *   "url": "http://localhost:81/4cace5c54f.png",
	 *   "domain": "http://localhost:81/"
	 * }
	 * </pre>
	 * </code>
	 * 
	 */
	protected PictShareResponse uploadImage(InputStream image, String format) {

		try (CloseableHttpClient httpclient = HttpClients.createDefault()) {

			HttpPost post = new HttpPost(config.getString("pictshare.post_url"));
			ContentBody cbFile = new InputStreamBody(image, "test." + format);
			HttpEntity entity = MultipartEntityBuilder.create().addPart("postimage", cbFile).build();
			post.setEntity(entity);

			try (CloseableHttpResponse result = httpclient.execute(post)) {
				return mapper.readValue(result.getEntity().getContent(), PictShareResponse.class);
			}
		} catch (IOException e) {
			throw new RuntimeException("Can't load the image", e);
		}
	}

	@Data
	public static final class PictShareResponse {
		String status;
		String type;
		String hash;
		String url;
		String domain;
		String reason;
	}
}
