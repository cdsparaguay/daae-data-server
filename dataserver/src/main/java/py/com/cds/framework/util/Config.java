package py.com.cds.framework.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.CDI;
import org.apache.commons.lang3.Validate;

/**
 * Bean que facilita la confiravés de un archivo en el Sistema.
 *
 * @author Arturo Volpe
 *
 */
@ApplicationScoped
public class Config {

	/**
	 * Limita la cantidad de opciones que tiene el autocomplete.
	 */
	private static final Integer LIMIT_AUTO_COMPLETE = 5;

	private static final String PROPERTY_FILE_LOCATION = "/";
	private static final String PROPERTY_FILE_NAME = "config.properties";

	private Properties properties;

	@PostConstruct
	public void init() {

		properties = new Properties();
		String configurationAbsolutePath = PROPERTY_FILE_LOCATION + System.getProperty("file.separator")
				+ PROPERTY_FILE_NAME;
		try (InputStream is = new FileInputStream(configurationAbsolutePath)) {
			properties.load(is);
		} catch (IOException fnfe) {
			System.out.println(
					"No se puede obtener archivo de configuración con path absoluto " + configurationAbsolutePath);
			System.out.println(
					"No se puede obtener archivo de configuración con path absoluto " + configurationAbsolutePath);
			System.out.println(
					"No se puede obtener archivo de configuración con path absoluto " + configurationAbsolutePath);
			System.out.println(
					"No se puede obtener archivo de configuración con path absoluto " + configurationAbsolutePath);

			System.out.println("Se busca en resources del war");
			ClassLoader classloader = Thread.currentThread().getContextClassLoader();
			try (InputStream stream = classloader.getResourceAsStream(PROPERTY_FILE_NAME)) {
				properties.load(stream);
			} catch (IOException e) {
				System.out.println(
						"No se puede obtener archivo de configuración desde resources, utilizando valores por defecto");
			}
		}

	}

	private static Config INSTANCE;

	public static Config getStatic() {

		if (INSTANCE == null) {
			INSTANCE = CDI.current().select(Config.class).get();
		}
		return INSTANCE;
	}

	public Integer getLimiteAutoComplete() {

		return getInteger("rest.result.limit", LIMIT_AUTO_COMPLETE);
	}

	public boolean isDevel() {

		return getBoolean("debug.enabled", false);
	}

	public String getString(String key) {

		String toRet = properties.getProperty(Validate.notNull(key, "Can't search with null key"));
		return Validate.notNull(toRet, "Can't find the key %s", key).trim();
	}

	/**
	 * Returns a integer property
	 *
	 * @param key
	 *            key of the property, can't be <code>null</code>
	 * @param def
	 *            default value, can be <code>null</code>
	 * @return if the value is found, the parsed value, if not, the default
	 *         value.
	 */
	public Integer getInteger(String key, Integer def) {

		String current = properties.getProperty(Validate.notNull(key, "Can't search with null key"));
		if (current == null) {
			return def;
		} else {
			return Integer.parseInt(current.trim());
		}

	}

	/**
	 * Returns a boolean property
	 *
	 * @param key
	 *            key of the property, can't be <code>null</code>
	 * @param def
	 *            default value, can be <code>null</code>
	 * @return if the value is found, the parsed value, if not, the default
	 *         value.
	 */
	public Boolean getBoolean(String key, Boolean def) {

		String current = properties.getProperty(Validate.notNull(key, "Can't search with null key"));
		if (current == null) {
			return def;
		} else {
			return Boolean.parseBoolean(current.trim());
		}

	}

	public String getString(String key, String de) {

		String toRet = properties.getProperty(Validate.notNull(key, "Can't search with null key"));
		return toRet == null ? de : toRet;
	}

	public void set(String string, String value) {

		this.properties.put(string, value);

	}

	public int getSessionTimeout() {

		return getInteger("session.timeout", 50000);
	}

	public int getSessionTtl() {

		return getInteger("session.ttl", 60);
	}

	public int getSessionMaxRequest() {

		return getInteger("session.limit", 100);
	}

	/**
	 * Builds or get a instance without the CDI container;
	 *
	 * @return
	 */
	public static Config getStaticWithoutCDI() {

		if (INSTANCE == null) {
			Config c = new Config();
			c.init();
			INSTANCE = c;
		}
		return INSTANCE;
	}

}
