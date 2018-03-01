package py.com.cds.framework.util;

import lombok.Value;

/**
 * Clase inmutable que almacena información acerca de ordenamiento.
 * 
 * @author Arturo Volpe
 * @since 1.0
 * @version 1.0 Sep 5, 2014
 * 
 */
@Value
public class SortInfo {

	/**
	 * Atributo por el cual se ordena, puede ser nested.
	 */
	String field;

	/**
	 * Define si es ascendente o descendente.
	 */
	boolean asc;

}
