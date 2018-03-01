package py.com.cds.framework.jpa;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = -55665915706744066L;

	/**
	 * Retorna el identificador de esta entidad.
	 * 
	 * @return
	 */
	public abstract long getId();

	public abstract void setId(long id);

	@Override
	public int hashCode() {

		final int prime = 31;
		long result = 1;
		long id = getId();
		result = prime * result + id;
		result += getClass().hashCode();
		return (int) result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		/**
		 * Si es la misma referencia necesariamente es igual.
		 */
		if (this == obj) {
			return true;
		}

		/**
		 * Nos aseguramos que sea asignable desde la clase actual, esto
		 * significa que es una subclase o de la misma clase.
		 */
		if (obj.getClass().isAssignableFrom(getClass())) {
			BaseEntity other = (BaseEntity) obj;
			long id = getId();
			long ot = other.getId();
			return ot == id;

		}

		return false;
	}

	/**
	 * Checks if the other is the same class and has the same id.
	 * 
	 * @param other
	 * @return
	 */
	public boolean equalsId(BaseEntity other) {
		if (other.getClass() != getClass())
			return false;

		return getId() == other.getId();

	}

	@Override
	public String toString() {
		return String.format("%s[id=%d]", getClass().getSimpleName(), getId());
	}

	@JsonIgnore
	public boolean isNew() {
		return getId() == 0l;
	}

}
