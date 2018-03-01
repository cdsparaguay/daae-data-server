/*
 * @SortInterpreter.java 1.0 Sep 5, 2014
 * Sistema Integral de Gestion Hospitalaria
 * 
 */
package py.com.cds.framework.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Sep 5, 2014
 * 
 */
public class Utils {

	public static final String ORDER_FORMAT_ASC = "asc";
	public static final String ORDER_FORMAT_DES = "des";
	public static final String ORDER_PATTERN = "(.*)\\.(.*)";

	/**
	 * Retorna un mapa de ordenes dada una lista serializada.
	 * 
	 * <p>
	 * Dado:
	 * 
	 * <pre>
	 * 		"nombre.asc"
	 * 		"apellido.des"
	 * 		"documento.descripcion.des"
	 * </pre>
	 * 
	 * Retorna:
	 * 
	 * <pre>
	 * 		"nombre" : true
	 * 		"apellido" : true
	 * 		"documento.descripcion" : false
	 * </pre>
	 * 
	 * @param sorts
	 * @return
	 */
	public static List<SortInfo> deserializeSort(List<String> sorts) {

		if (sorts == null || sorts.isEmpty())
			return new ArrayList<>();

		Pattern p = Pattern.compile(ORDER_PATTERN);
		ArrayList<SortInfo> toRet = new ArrayList<>();
		for (String s : sorts) {
			if (StringUtils.isEmpty(s))
				continue;
			Matcher matcher = p.matcher(s);
			if (!matcher.matches()) {
				throw new RuntimeException("Malformated order " + s);
			}

			String field = matcher.group(1);
			String order = matcher.group(2);

			toRet.add(new SortInfo(field, ORDER_FORMAT_ASC.equals(order)));

		}
		return toRet;
	}

	public static Map<String, String> joinAndCheckLists(List<String> properties, List<String> values,
			Class<?> entityClass) {

		if (properties == null || properties.isEmpty() || values == null || values.isEmpty())
			return Collections.emptyMap();

		if (properties.size() != values.size())
			throw new RuntimeException("Diferentes ordenes de propiedades y valores");

		Map<String, String> toRet = new HashMap<>();
		for (int i = 0; i < properties.size(); i++) {
			toRet.put(properties.get(i), values.get(i));
		}
		return toRet;
	}

	/**
	 * Retorna un {@link Date} que representa el último dia del mes y anho.
	 * 
	 * @param anho
	 *            anho del cual se desea su primer dia
	 * @param mes
	 *            mes del anho del cual se desea su primer dia
	 * @return {@link Date} representando el último dia, nunca <code>null</code>
	 */
	public static Date getDateFin(int anho, int mes) {
		Calendar fin = Calendar.getInstance();
		fin.clear();
		fin.set(anho, mes, 1);
		fin.add(Calendar.MONTH, 1);
		fin.getTime();
		fin.add(Calendar.DATE, -1);
		return fin.getTime();
	}

	/**
	 * Retorna un {@link Date} que representa el primer dia del mes y anho.
	 * 
	 * Notar que el mes que se pasa aquí empieza en 0
	 * 
	 * @param anho
	 *            anho del cual se desea su primer dia
	 * @param mes
	 *            mes del anho del cual se desea su primer dia
	 * @return {@link Date} representando el primer dia, nunca <code>null</code>
	 */
	public static Date getDateInicio(int anho, int mes) {
		Calendar inicio = Calendar.getInstance();
		inicio.clear();
		inicio.set(anho, mes, 1);
		return inicio.getTime();
	}
	public static Date getNextMonth(int anho, int mes) {
		Calendar inicio = Calendar.getInstance();
		inicio.clear();
		inicio.set(anho, mes, 1);
		inicio.add(Calendar.MONTH, 1);
		Date dInicio = inicio.getTime();
		return dInicio;
	}
}
