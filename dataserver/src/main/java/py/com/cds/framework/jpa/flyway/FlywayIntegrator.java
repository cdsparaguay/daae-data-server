package py.com.cds.framework.jpa.flyway;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.hibernate.boot.Metadata;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * Ejecuta una migracion.
 * 
 * @author Arturo Volpe
 *
 */
public class FlywayIntegrator implements Integrator {

	/**
	 * Si la primera vez se ejecuta, y falla con el mensaje
	 * "foudn not empty schema", se ejecuta un baseline.
	 */
	boolean firstTime = true;

	@Override
	public void integrate(Metadata metadata, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {

		Flyway fl = new Flyway();
		fl.setCallbacks(new AuditCallback());
		fl.setDataSource(getDataSource(sessionFactory));

		try {
			fl.migrate();
		} catch (RuntimeException e) {

			if (firstTime && e.getMessage().startsWith("Found non-empty schema")) {
				System.out.println("Aplicando baseline");
				fl.baseline();
				firstTime = false;
				this.integrate(metadata, sessionFactory, serviceRegistry);
			} else {
				throw e;
			}
		}
	}

	@Override
	public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
	}

	private DataSource getDataSource(SessionFactoryImplementor factory) {
		ConnectionProvider provider = factory.getServiceRegistry().getService(ConnectionProvider.class);
		DatasourceConnectionProviderImpl dsProvider = (DatasourceConnectionProviderImpl) provider;
		return dsProvider.getDataSource();
	}

}
