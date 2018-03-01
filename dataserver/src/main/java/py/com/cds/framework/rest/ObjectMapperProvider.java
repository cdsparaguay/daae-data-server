package py.com.cds.framework.rest;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.geolatte.geom.Polygon;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import py.com.cds.framework.util.GeoJSONToPolygon;
import py.com.cds.framework.util.PolygonToGeoJSON;

@Provider
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class ObjectMapperProvider implements ContextResolver<ObjectMapper> {

	private final ObjectMapper objectMapper;

	public ObjectMapperProvider() {
		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new Hibernate4Module());
		objectMapper.registerModule(new Jdk8Module());

		// register GeoJSON module
		SimpleModule sm = new SimpleModule("test");
		sm.addDeserializer(Polygon.class, new GeoJSONToPolygon());
		sm.addSerializer(Polygon.class, new PolygonToGeoJSON());

		objectMapper.registerModule(sm);

	}

	@Override
	public ObjectMapper getContext(Class<?> objectType) {
		return objectMapper;
	}

	@javax.enterprise.inject.Produces
	public ObjectMapper produces() {
		return objectMapper;
	}

}
