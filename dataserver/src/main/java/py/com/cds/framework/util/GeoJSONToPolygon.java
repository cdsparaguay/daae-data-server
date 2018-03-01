package py.com.cds.framework.util;

import java.io.IOException;

import org.geolatte.common.dataformats.json.to.GeoJsonTo;
import org.geolatte.common.dataformats.json.to.PolygonTo;
import org.geolatte.geom.LinearRing;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.PositionSequence;
import org.geolatte.geom.codec.db.sqlserver.CountingPositionSequenceBuilder;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.geolatte.geom.crs.ProjectedCoordinateReferenceSystem;
import org.geolatte.geom.crs.Unit;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GeoJSONToPolygon extends JsonDeserializer<Polygon> {

	@Override
	public Polygon deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {

		GeoJsonTo geom = p.readValueAs(GeoJsonTo.class);

		System.out.println(geom.getClass());

		if (geom instanceof PolygonTo) {
			return transform((PolygonTo) geom);
		}

		throw new IOException("La clase " + geom.getClass().getSimpleName() + " no esta soportada");
	}

	private Polygon transform(PolygonTo from) {

		ProjectedCoordinateReferenceSystem mkProjected = CoordinateReferenceSystems.mkProjected(Unit.METER);
		LinearRing[] rings = new LinearRing[from.getCoordinates().length];
		for (int i = 0; i < from.getCoordinates().length; i++) {
			rings[i] = new LinearRing(createPositionSequence(from.getCoordinates()[i], mkProjected), mkProjected);
		}
		return new Polygon(rings);
	}

	private PositionSequence createPositionSequence(double[][] coordinates, CoordinateReferenceSystem crs) {
		if (coordinates == null) {
			return null;
		} else if (coordinates.length == 0) {
			return new CountingPositionSequenceBuilder(crs).toPositionSequence();
		}

		CountingPositionSequenceBuilder psb = new CountingPositionSequenceBuilder(crs);
		for (double[] point : coordinates) {
			psb.add(point);
		}
		return psb.toPositionSequence();
	}
}
