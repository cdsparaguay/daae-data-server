package py.com.cds.framework.util;

import static org.torpedoquery.jpa.Torpedo.condition;
import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.orderBy;
import static org.torpedoquery.jpa.Torpedo.select;
import static org.torpedoquery.jpa.Torpedo.where;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.torpedoquery.jpa.OnGoingComparableCondition;
import org.torpedoquery.jpa.OnGoingLogicalCondition;
import org.torpedoquery.jpa.OnGoingStringCondition;
import org.torpedoquery.jpa.Query;
import org.torpedoquery.jpa.TorpedoFunction;

/**
 * 
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Sep 5, 2014
 * 
 */
public class QueryHelper {

	/**
	 * Clase que permite interceptar la creación de la consulta, y permite
	 * modificar los filtros y la ordenación.
	 * 
	 * @param <T>
	 *            clase origen, la misma que el from.
	 */
	public static class QueryCustomizer<T> {

		/**
		 * Permite agregar condifiones al where.
		 * <p>
		 * El parámetro <code>from</code> es la raiz de la expresión.
		 * </p>
		 * 
		 * @param from
		 *            principal del query
		 * @return condición a la que se le concatenarán las demas condiciones
		 *         típicas del filtro.
		 */
		public OnGoingLogicalCondition firstWhere(T from) {
			return null;
		};

		/**
		 * Permite modificar los primeros orders.
		 */
		public void firstOrders(T from) {
		};

	}

	public <T> Query<T> get(int firstResult, int pageCount, List<SortInfo> sortInfo, Map<String, String> params,
			Class<T> classOfT, String globalSearch, QueryCustomizer<T> customizer) {

		if (customizer == null)
			customizer = new QueryCustomizer<>();
		T from = from(classOfT);

		applyWhere(params, classOfT, customizer, from);
		customizer.firstOrders(from);
		if (sortInfo != null)
			addOrders(from, sortInfo, classOfT);
		Query<T> query = select(from).setFirstResult(firstResult).setMaxResults(pageCount);
		return query;
	}

	private <T> void applyWhere(Map<String, String> params, Class<T> classOfT, QueryCustomizer<T> customizer, T from) {
		OnGoingLogicalCondition previous = customizer.firstWhere(from);

		if (params != null)
			for (Entry<String, String> entry : params.entrySet()) {
				OnGoingLogicalCondition current = getCondition(from, entry.getKey(), entry.getValue(), classOfT);
				if (current == null)
					continue;
				if (previous == null) {
					previous = where(current);
				} else {
					previous = previous.and(current);
				}
			}
	}

	public <T> Query<Long> getCount(Map<String, String> params, Class<T> classOfT, QueryCustomizer<T> customizer) {

		if (customizer == null)
			customizer = new QueryCustomizer<>();

		T from = from(classOfT);
		applyWhere(params, classOfT, customizer, from);

		return select(TorpedoFunction.count(from));

	}

	protected <T> OnGoingLogicalCondition doComplexPath(T from, String propertyName, String compareValue,
			Class<?> clazz) throws Exception {

		Pair<Method, Object> info = getMethodAndObjectInvokeThroug(propertyName, clazz, from);
		return getCondition(info.getValue(), compareValue, info.getKey());

	}

	/**
	 * Este método provee una funcionalidad similar al Resolver de
	 * apache-beanutils.
	 * 
	 * <p>
	 * La cadena de nodos de un propiedad como:
	 * 
	 * <pre>
	 * ciudad.pais.nombre
	 * </pre>
	 * 
	 * Esta llena de proxies, los cuales deben ser accedidos si se quiere
	 * utilizar el proxy para realizar comparaciones utilizando torpedo.
	 * 
	 * </p>
	 * <p>
	 * Este código debe provocar el mismo resultado, que se si hiciese:
	 * 
	 * <pre>
	 * String torpedoProxyDelTipoDeNombre = ciudad.getPais().getNombre()
	 * </pre>
	 * 
	 * De esta forma, con el objeto retornado, se puede hacer lo siguiente
	 * 
	 * <pre>
	 * where(torpedoProxyDelTipoNombre).eq(lower(&quot;Paraguay&quot;));
	 * </pre>
	 * 
	 * Lo cual solo es posible, si el objeto retornado es del tipo proxy, que
	 * torpedo necesita.
	 * </p>
	 * 
	 * @param propertyName
	 *            propiedad a buscar, no debe ser nula y debe estar separada por
	 *            puntos.
	 * @param clazz
	 *            raiz de la expresión
	 * @param from
	 *            objeto del tipo <code>clazz</code> que representa el
	 *            <code>from</code> de torpedo
	 * @return par, donde el key es el getter y el valor es el objeto raiz de
	 *         ese getter.
	 * @throws Exception
	 *             excepción de reflexión
	 */
	private Pair<Method, Object> getMethodAndObjectInvokeThroug(String propertyName, Class<?> clazz, Object from)
			throws Exception {
		String[] parts = propertyName.split("\\.");
		Class<?> lastClass = clazz;
		Object lastRoot = from;
		Method getter = null;
		for (int i = 0; i < parts.length - 1; i++) {

			String s = parts[i];
			getter = getGetter(s, lastClass);
			lastClass = getter.getReturnType();
			lastRoot = getter.invoke(lastRoot);
		}

		getter = getGetter(parts[parts.length - 1], lastClass);
		return new ImmutablePair<>(getter, lastRoot);
	}

	protected <T> OnGoingLogicalCondition doSimplePath(T from, String propertyName, String compareValue, Class<?> clazz)
			throws Exception {

		return getCondition(from, compareValue, getGetter(propertyName, clazz));
	}

	public void addOrders(Object from, List<SortInfo> sortInfo, Class<?> parent) {

		try {
			for (SortInfo si : sortInfo) {
				addOrder(from, si, parent);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);

		}

	}

	private void addOrder(Object from, SortInfo si, Class<?> parent) throws Exception {

		Pair<Method, Object> info = getMethodAndObjectInvokeThroug(si.getField(), parent, from);
		if (si.isAsc())
			orderBy(TorpedoFunction.asc(info.getKey().invoke(info.getValue())));
		else
			orderBy(TorpedoFunction.desc(info.getKey().invoke(info.getValue())));
	}

	/**
	 * Retorna el Getter de una propiedad especifica de una clase.
	 * 
	 * Se podría buscar en apache-commons algún PropertyUtils que ya contenga
	 * una cache de valores.
	 * 
	 * </p>
	 * 
	 * @param propertyName
	 * @param clazz
	 * @return
	 * @throws IntrospectionException
	 */
	private Method getGetter(String propertyName, Class<?> clazz) throws IntrospectionException {
		BeanInfo beaninfo = Introspector.getBeanInfo(clazz);
		PropertyDescriptor[] properties = beaninfo.getPropertyDescriptors();
		for (PropertyDescriptor pd : properties) {
			if (pd.getName().equals(propertyName))
				return pd.getReadMethod();
		}
		throw new IntrospectionException("Cant get the getter of " + propertyName + " of class: " + clazz.getName());
	}

	protected <T> OnGoingLogicalCondition getCondition(T object, String propertyName, String compareTo,
			Class<T> parent) {

		try {
			if (propertyName.contains("."))
				return doComplexPath(object, propertyName, compareTo, parent);

			return doSimplePath(object, propertyName, compareTo, parent);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Realiza la comparación entre dos atributos utilizando los proxies de
	 * torpedo.
	 * 
	 * <p>
	 * Como en el ejemplo mostrado en {@link #getCompareValue(Class, String)},
	 * se puede utilizar esté metodo para realizar una comparación entre fechas
	 * como sigue:
	 * 
	 * <pre>
	 * if (getter.getReturnType() == Date.class) {
	 * 	OnGoingComparableCondition&lt;Date&gt; comp = condition(getter.invoke(root));
	 * 	Pair&lt;Date, Date&gt; dates = getCompareValue(getter.getReturnType(), object);
	 * 	return comp.between(dates.key(), dates.value());
	 * }
	 * </pre>
	 * 
	 * @param root
	 *            raiz de la condición actual.
	 * @param object
	 *            objeto a comparar, es un string, por que todo se serializa.
	 * @param getter
	 *            de la propiedad.
	 * @return condicion que puede ser encadenada a otra.
	 * @throws Exception
	 *             reflexión exception
	 */
	private OnGoingLogicalCondition getCondition(Object root, String object, Method getter) throws Exception {

		if (getter.getReturnType() == String.class) {
			OnGoingStringCondition<String> comp = condition(TorpedoFunction.lower((String) getter.invoke(root)));
			return comp.like().any(object.toLowerCase());
		}

		if (object.contains("yadcf_delim")) {

			return handleBetween(root, object, getter);
		}

		return condition(getter.invoke(root)).eq(getCompareValue(getter.getReturnType(), object));
	}

	/**
	 * Metodo que retorna el valor a comprar.
	 * 
	 * <p>
	 * Esté metodo, junto con {@link #getCondition(Object, String, Method)} son
	 * los que definen como se compara un atributo, los demás métodos se centran
	 * en como obtener log getters honrando a Torpedo.
	 * </p>
	 * <p>
	 * Aquí se pueden tener convenciones especiales para ciertos tipos de
	 * atributos, por ejemplo, si queremos comparar por rango de fechas, en
	 * value debería venir (si configuramos primefaces), algo como YYYY-MM-DD a
	 * YYYY-MM-DD, entonces aquí (junto con
	 * {@link #getCondition(Object, String, Method)}) deberiamos parsear esa
	 * cadena, y retornar las dos fechas.
	 * </p>
	 * 
	 * @param classType
	 *            clase de retorno del objeto deseado
	 * @param value
	 *            valor serializado
	 * @return valor convertido.
	 */
	protected Object getCompareValue(Class<?> classType, String value) {

		if (classType == String.class)
			return value;
		if (classType == Integer.class)
			return Integer.valueOf(value);
		if (classType == Long.class)
			return Long.valueOf(value);
		if (classType == long.class)
			return Long.valueOf(value);
		if (classType == Double.class)
			return Double.valueOf(value);
		if (classType == BigDecimal.class)
			return new BigDecimal(value);
		if (classType == BigInteger.class)
			return new BigInteger(value);
		if (classType.isEnum()) {
			if (StringUtils.isEmpty(value))
				throw new IllegalArgumentException("No se puede comparar un enum con un valor vacio");
			value = value.toUpperCase();
			for (Object o : classType.getEnumConstants()) {
				if (o.toString().equals(value))
					return o;
			}
		}
		if (classType == boolean.class || classType == Boolean.class) {

			if ("SI".equals(value))
				return true;
			if ("NO".equals(value))
				return false;
			return Boolean.valueOf(value);
		}
		// TODO add more types.

		throw new RuntimeException("Add a getCompareValue al tipo " + classType.getName());
	}

	private OnGoingLogicalCondition handleBetween(Object root, String object, Method getter) throws Exception {

		String[] parts = object.split("-yadcf_delim-");
		if (parts.length == 0)
			return null;
		String first = parts[0];
		String last = parts.length > 1 ? parts[1] : null;

		if (getter.getReturnType() == Date.class) {
			return handleDateComparision(root, object, getter, first, last);
		}
		if (Number.class.isAssignableFrom(getter.getReturnType()) || long.class == getter.getReturnType()) {
			return buildNumberBetween(root, getter, first, last);
		}

		throw new RuntimeException("Agrega un #handleBetween para el tipo: " + getter.getReturnType());
	}

	@SuppressWarnings("unchecked")
	protected <T extends Number> OnGoingLogicalCondition buildNumberBetween(Object root, Method getter, String first,
			String last) throws IllegalAccessException, InvocationTargetException {

		if (!StringUtils.isEmpty(first) && StringUtils.isEmpty(last)) {
			OnGoingComparableCondition<T> cond = condition((Comparable<T>) getter.invoke(root));
			return cond.gte((T) getCompareValue(getter.getReturnType(), first));
		}
		if (StringUtils.isEmpty(first) && !StringUtils.isEmpty(last)) {
			OnGoingComparableCondition<T> cond = condition((Comparable<T>) getter.invoke(root));
			return cond.lte((T) getCompareValue(getter.getReturnType(), last));
		}
		return condition((Comparable<T>) getter.invoke(root)).gte((T) getCompareValue(getter.getReturnType(), first))
				.and((Comparable<T>) getter.invoke(root))
				.lte((T) getCompareValue(getter.getReturnType(), last));
	}

	private OnGoingLogicalCondition handleDateComparision(Object root, String object, Method getter, String first,
			String last) throws Exception {

		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();

		if (!StringUtils.isEmpty(last)) {
			c.setTime(sdf.parse(last));
			c.add(Calendar.DATE, 1);
		}
		if (!StringUtils.isEmpty(first) && StringUtils.isEmpty(last)) {
			return condition((Date) getter.invoke(root)).gte(sdf.parse(first));
		}
		if (StringUtils.isEmpty(first) && !StringUtils.isEmpty(last)) {
			return condition((Date) getter.invoke(root)).lte(c.getTime());
		}
		return condition((Date) getter.invoke(root)).between((sdf.parse(first)), c.getTime());
	}
}
