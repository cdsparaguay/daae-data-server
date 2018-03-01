package py.com.cds.framework.ejb;

import static org.torpedoquery.jpa.Torpedo.from;
import static org.torpedoquery.jpa.Torpedo.select;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.Validate;
import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;

import lombok.AccessLevel;
import lombok.Getter;
import py.com.cds.framework.jpa.BaseEntity;
import py.com.cds.framework.util.Config;
import py.com.cds.framework.util.DataWithCount;
import py.com.cds.framework.util.QueryHelper;
import py.com.cds.framework.util.SortInfo;
import py.com.cds.framework.util.Utils;

public class BaseLogic<T extends BaseEntity> {

	@PersistenceContext
	@Getter(AccessLevel.PROTECTED)
	private EntityManager em;

	@Inject
	@Getter(AccessLevel.PROTECTED)
	private JinqJPAStreamProvider streams;

	@Inject
	@Getter(AccessLevel.PROTECTED)
	private Config config;

	@Getter(AccessLevel.PUBLIC)
	private Class<T> classOfT;

	@SuppressWarnings("unchecked")
	public BaseLogic() {

		Class<?> thisClass = getClass();
		if (thisClass.getName().contains("$"))
			thisClass = thisClass.getSuperclass();
		Type superClass = thisClass.getGenericSuperclass();
		ParameterizedType type = (ParameterizedType) superClass;

		classOfT = (Class<T>) type.getActualTypeArguments()[0];
		if (classOfT == null)
			throw new RuntimeException("Cant get the parameterizedClass of claas " + getClass().getName());

	}

	public List<T> getAll() {

		return select(from(getClassOfT())).list(em);
	}

	/**
	 * Agrega un objeto.
	 * <p>
	 * Si su identificador es del tipo String, lo convierte a minusculas, para
	 * evitar que dos identificadores similares se persistan en la base de datos
	 * </p>
	 * 
	 * @param entity
	 * @return
	 */
	public T add(T entity) {
		Validate.notNull(entity, "No se puede agregar una entidad nula");
		return em.merge(entity);
	}

	public T remove(T entity) {

		return remove(entity.getId());
	}

	public T remove(long key) {
		T entity = findById(key);
		em.remove(entity);
		return entity;
	}

	public DataWithCount<T> get(int firstResult, int pageCount, List<String> orders, List<String> values,
			List<String> columns, String globalSearch) {

		List<SortInfo> sortInfo = Utils.deserializeSort(orders);
		Map<String, String> params = Utils.joinAndCheckLists(columns, values, getClassOfT());
		return new DataWithCount<>(new QueryHelper()
				.get(firstResult, pageCount, sortInfo, params, getClassOfT(), globalSearch, null).list(em),
				getCount(params), getCount(Collections.emptyMap()), 0);
	}

	public long getCount(Map<String, String> params) {

		return new QueryHelper().getCount(params, getClassOfT(), null).get(em);

	}

	public Optional<T> getById(Long key) {

		return getById(key, getClassOfT());
	}

	private <U> Optional<U> getById(Long id, Class<U> clazz) {

		Validate.notNull(id, "Entidad con id nulo no encontrada");
		U toRet = em.find(clazz, id);

		if (toRet == null)
			return Optional.empty();
		else
			return Optional.of(toRet);
	}

	/**
	 * Retorna una entidad, si la misma no existe se lanza una excepcion.
	 * 
	 * @param key
	 * @return la entidad o <code>null</code>
	 */
	public T findById(Long key) {

		return findById(key, getClassOfT());
	}

	/**
	 * Retorna una entidad, si la misma no existe se lanza una excepcion.
	 * 
	 * @param key
	 * @return la entidad o <code>null</code>
	 */
	public <U> U findById(Long key, Class<U> clazz) {

		return getById(key, clazz).orElseThrow(() -> new IllegalArgumentException(
				"Entidad del tipo " + clazz.getSimpleName() + " no encontrada con id " + key));
	}

	public T update(T entity) {

		findById(entity.getId());
		return em.merge(entity);

	}

	protected JPAJinqStream<T> getStreamAll() {
		return getStreamAll(getClassOfT());
	}

	protected <U> JPAJinqStream<U> getStreamAll(Class<U> clazz) {
		return streams.streamAll(getEm(), clazz);
	}

	/**
	 * Retorna un query con el tipo de dato de esta lógica.
	 * 
	 */
	protected TypedQuery<T> createQuery(String query) {
		return getEm().createQuery(query, getClassOfT());
	}

}
