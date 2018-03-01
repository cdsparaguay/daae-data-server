package py.com.cds.framework.security.logic;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
@Documented
@Inherited
public @interface Logged {

	public static final String DEFAULT_KEY = "DEFAULT";

	/**
	 * Requiere alguno de estos roles.
	 * 
	 * @return los roles que necesita, por defecto CARGADOR
	 */
	Rol[] value() default Rol.CARGADOR;

	/**
	 * Define si un método es público, al no tener ningúna anotación, se asume
	 * que requiere rol {@link Rol#CARGADOR}
	 * 
	 * @return <code>true</code> si cualquier puede acceder al método,
	 *         <code>false</code> en caso contrario (por defecto es
	 *         <code>false</code>).
	 */
	boolean isPublic() default false;

	public static enum Rol {

		CARGADOR, RESPONDEDOR, ADMINISTRADOR
	}
}
