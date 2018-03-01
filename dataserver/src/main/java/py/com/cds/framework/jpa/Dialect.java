package py.com.cds.framework.jpa;

import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.spatial.dialect.postgis.PostgisDialect;
import org.hibernate.type.StandardBasicTypes;

public class Dialect extends PostgisDialect {

	private static final long serialVersionUID = 6881438207140668433L;

	public Dialect() {
		super();
		especializedTypes();
	}

	protected void especializedTypes() {

		registerFunction("distanceMeters", new StandardSQLFunction("st_distance_sphere", StandardBasicTypes.DOUBLE));
		registerFunction("toGeoJson", new StandardSQLFunction("ST_AsGeoJson", StandardBasicTypes.STRING));
	}
}
