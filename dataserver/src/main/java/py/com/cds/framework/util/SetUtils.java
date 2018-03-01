package py.com.cds.framework.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import lombok.Value;

public class SetUtils {

	public static final String RUC_SIN_NOMBRE = "44444401-7";
	public static final String RAZON_SOCIAL_SIN_NOMBRE = "IMPORTES CONSOLIDADOS";

	public static int getDigitoVerificador(String ruc) {
		return getDigitoVerificador(ruc, 11);
	}

	public static int getDigitoVerificador(String ruc, int base) {

		int k = 2;
		int total = 0;

		String alRevez = invertirCadena(eliminarNoDigitos(ruc));

		for (char numero : alRevez.toCharArray()) {
			total += (numero - '0') * k++;

			if (k > base)
				k = 2;
		}

		int resto = total % 11;

		return resto > 1 ? 11 - resto : resto;
	}

	protected static String invertirCadena(String ruc) {
		// si se dispone de apache commons se puede usar
		// StringUtils.reverse(ruc);
		return new StringBuilder(ruc).reverse().toString();
	}

	/**
	 * Elimina todos los no digitos de la cadena.
	 * 
	 * @param ruc
	 *            ruc con numeros, simbolos y letras.
	 * @return una versión del ruc consistente de solo digitos.
	 */
	protected static String eliminarNoDigitos(String ruc) {
		String toRet = "";
		for (char c : ruc.toCharArray()) {
			if (Character.isDigit(c)) {
				toRet += c;
			} else {
				toRet += (int) c;
			}
		}
		return toRet;
	}

	public static RUC getRuc(String fullRuc, String razon) {

		if (StringUtils.isBlank(fullRuc) || fullRuc.indexOf("-") < 0)
			return getRuc(RUC_SIN_NOMBRE, RAZON_SOCIAL_SIN_NOMBRE);

		if ("-1".equals(fullRuc))
			return getRuc(RUC_SIN_NOMBRE, RAZON_SOCIAL_SIN_NOMBRE);

		if (fullRuc.endsWith("-"))
			return getRuc(RUC_SIN_NOMBRE, RAZON_SOCIAL_SIN_NOMBRE);

		String[] parts = fullRuc.split("-");
		if (parts.length == 2)
			return new RUC(parts[0], parts[1], razon);
		throw new IllegalArgumentException("No se puede procesar el ruc: " + fullRuc);

	}

	public static RUC getDefaultRUC() {
		return getRuc(RUC_SIN_NOMBRE, RAZON_SOCIAL_SIN_NOMBRE);
	}

	/**
	 * Calcula el iva.
	 * 
	 * @param total
	 * @param iva
	 * @return
	 */
	public static MontoConIva calcularImpuesto(Long total, int iva) {

		Validate.isTrue(iva == 5 || iva == 10, "Iva invalido");
		Validate.notNull(total, "Monto invalido");

		int aDividir = iva == 10 ? 11 : 21;
		double ivaCaculado = total.doubleValue() / aDividir;
		long ivaRedondeado = Math.round(ivaCaculado);

		long montoCalculado = total - ivaRedondeado;

		return new MontoConIva(montoCalculado, ivaRedondeado, iva);
	}

	@Value
	public static class RUC {
		public boolean isGenerico() {

			return RAZON_SOCIAL_SIN_NOMBRE.equals(getRazonSocial());
		}

		String ruc;
		String dv;
		String razonSocial;
	}

	@Value
	public static class MontoConIva {
		long monto;
		long iva;
		int porcentajeIva;
	}
}
