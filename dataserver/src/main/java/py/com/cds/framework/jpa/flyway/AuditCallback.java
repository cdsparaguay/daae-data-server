package py.com.cds.framework.jpa.flyway;

import java.sql.Connection;

import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.callback.FlywayCallback;

import py.com.cds.framework.jpa.audit.AuditUtil;

/**
 * Crea tablas de auditoria
 * 
 * @author Arturo Volpe
 *
 */
public class AuditCallback implements FlywayCallback {

	@Override
	public void beforeClean(Connection connection) {
	}

	@Override
	public void afterClean(Connection connection) {
	}

	@Override
	public void beforeMigrate(Connection connection) {
	}

	@Override
	public void afterMigrate(Connection connection) {

		try {
			System.out.println("Actualizando datos de auditoria");
			new AuditUtil().updateAuditInformation(connection);

		} catch (Exception e) {
			throw new RuntimeException("Error al intentar realizar la migracion", e);
		}
	}

	@Override
	public void beforeEachMigrate(Connection connection, MigrationInfo info) {
	}

	@Override
	public void afterEachMigrate(Connection connection, MigrationInfo info) {
	}

	@Override
	public void beforeValidate(Connection connection) {
	}

	@Override
	public void afterValidate(Connection connection) {
	}

	@Override
	public void beforeBaseline(Connection connection) {
	}

	@Override
	public void afterBaseline(Connection connection) {
	}

	@Override
	public void beforeRepair(Connection connection) {
	}

	@Override
	public void afterRepair(Connection connection) {
	}

	@Override
	public void beforeInfo(Connection connection) {
	}

	@Override
	public void afterInfo(Connection connection) {
	}

}
