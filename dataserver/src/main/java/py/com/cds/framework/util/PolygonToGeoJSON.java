package py.com.cds.framework.util;

import java.io.IOException;

import org.geolatte.common.dataformats.json.jackson.JsonMapper;
import org.geolatte.common.dataformats.json.jackson.PolygonSerializer;
import org.geolatte.geom.C2D;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.LineString;
import org.geolatte.geom.Polygon;
import org.geolatte.geom.Position;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PolygonToGeoJSON extends PolygonSerializer {

	public PolygonToGeoJSON() {
		super(new JsonMapper());
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void writeShapeSpecificSerialization(Polygon value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException {

		jgen.writeFieldName("type");
		jgen.writeString("Polygon");
		jgen.writeArrayFieldStart("coordinates");
		JsonSerializer<Object> ser = provider.findValueSerializer(Double.class, null);
		writeLineString(value.getExteriorRing(), jgen, provider, ser);
		for (int i = 0; i < value.getNumInteriorRing(); i++) {
			writeLineString(value.getInteriorRingN(i), jgen, provider, ser);
		}

		jgen.writeEndArray();
	}

	/**
	 * Writes a ring to the json
	 */
	protected void writeLineString(LineString<C2D> line, JsonGenerator jgen, SerializerProvider provider,
			JsonSerializer<Object> ser) throws IOException {

		jgen.writeStartArray();
		for (int i = 0; i < line.getNumPositions(); i++) {

			jgen.writeStartArray();
			C2D position = line.getPositionN(i);
			ser.serialize(position.getX(), jgen, provider);
			ser.serialize(position.getY(), jgen, provider);
			jgen.writeEndArray();
		}

		jgen.writeEndArray();
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected double[] envelopeToCoordinates(Envelope e) {

		Position upperLeft = e.upperLeft();
		Position lowerRigth = e.lowerRight();
		return new double[] { lowerRigth.getCoordinate(0), lowerRigth.getCoordinate(1), upperLeft.getCoordinate(0),
				upperLeft.getCoordinate(1) };
	}
}
