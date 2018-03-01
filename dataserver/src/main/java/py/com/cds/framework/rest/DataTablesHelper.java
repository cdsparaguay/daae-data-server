package py.com.cds.framework.rest;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@ApplicationScoped
public class DataTablesHelper {

	/**
	 * La posición del primer número en la cadena
	 *
	 * <pre>
	 * columns[0][name]
	 * </pre>
	 */
	public static final int IN_COLUMN_INDEX_POSITION = 8;

	/**
	 * La posición del primer número en la cadena
	 *
	 * <pre>
	 * order[0][name]
	 * </pre>
	 */
	public static final int IN_ORDER_INDEX_POSITION = 6;

	/**
	 * Extrae la informacion de un query de datatables.
	 * 
	 * @param queryParams
	 *            query de datatables.
	 * @return un objecto con tres propiedades, los atributos, los filtros, y
	 *         los ordenes.
	 */
	public QueryExtracted extractData(MultivaluedMap<String, String> queryParams) {
		QueryExtracted qe = new QueryExtracted();
		for (String key : queryParams.keySet()) {
			if (key.contains("[data]")) {
				String property = queryParams.getFirst(key);
				// FIXME permitir múltiples filtros
				if (!qe.getProperties().contains(property)) {
					Integer index = Character.getNumericValue(key.charAt(IN_COLUMN_INDEX_POSITION));
					String queryValue = "columns[" + index + "][search][value]";
					String value = queryParams.getFirst(queryValue);
					if (!StringUtils.isEmpty(value)) {
						qe.getProperties().add(property);
						qe.getValues().add(queryParams.getFirst(queryValue));
					}
				}
			} else if (key.startsWith("order[") && (key.endsWith("][column]"))) {
				String propertyIndex = queryParams.getFirst(key);
				Integer orderIndex = Character.getNumericValue(key.charAt(IN_ORDER_INDEX_POSITION));

				String toGetName = "columns[" + propertyIndex + "][data]";
				String property = queryParams.getFirst(toGetName);
				String orderQuery = "order[" + orderIndex + "][dir]";
				String direction = queryParams.getFirst(orderQuery);

				qe.getOrders().add(property + "." + direction);
			}
		}
		return qe;
	}

	@Data
	public static class QueryExtracted {
		List<String> properties = new ArrayList<>();
		List<String> values = new ArrayList<>();
		List<String> orders = new ArrayList<>();
	}

}
