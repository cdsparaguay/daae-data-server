package py.com.cds.framework.jpa.audit;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class AuditUtil {

	//@formatter:off
	private static final String QUERY_TABLES = 
			" SELECT table_schema || '.' || table_name AS table"
			+ " FROM information_schema.tables" 
			+ " WHERE table_schema NOT IN ('pg_catalog', 'information_schema')"
			+ "	AND table_type = 'BASE TABLE'";
	
	
	private static final String QUERY_ADD_TRIGGER = 
			  " DROP TRIGGER IF EXISTS ##TABLE##_audit ON ##SCHEMA##.##TABLE##; \n" 
			+ " CREATE TRIGGER ##TABLE##_audit"
			+ " AFTER INSERT OR UPDATE OR DELETE ON ##SCHEMA##.##TABLE##"
			+ " FOR EACH ROW EXECUTE PROCEDURE audit.if_modified_func();";
	//@formatter:on

	private static final List<String> IGNORED_TALBES = Arrays.asList("audit.logged_actions", "public.schema_version", "public.spatial_ref_sys");

	public void updateAuditInformation(Connection con) throws IOException, SQLException {

		updateTrigger(con);
		for (String table : getAllTables(con)) {
			addTriggerATable(con, table);
		}
	}

	public List<String> getAllTables(Connection con) throws SQLException {

		List<String> toRet = new ArrayList<>();
		ResultSet rs = con.prepareStatement(QUERY_TABLES).executeQuery();

		while (rs.next()) {
			String tableName = rs.getString(1);
			if (IGNORED_TALBES.contains(tableName)) {
				continue;
			}
			toRet.add(tableName);
		}

		rs.close();
		return toRet;
	}

	public void addTriggerATable(Connection con, String table) throws SQLException {

		String schemaName = table.split("\\.")[0];
		String tableName = table.split("\\.")[1];

		String query = QUERY_ADD_TRIGGER.replaceAll("##TABLE##", tableName).replaceAll("##SCHEMA##", schemaName);

		con.prepareStatement(query).execute();
	}

	public void updateTrigger(Connection con) throws IOException, SQLException {

		String query = new String(
				IOUtils.toString(getClass().getClassLoader().getResourceAsStream("db/util/audit_trigger.sql")));

		con.prepareStatement(query).execute();

	}

}
