package py.com.cds.framework.rest;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.persistence.Entity;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonSchemaGenerator {

	public ObjectNode getSchema(Class<?> clazz) {

		JsonNodeFactory jnf = JsonNodeFactory.instance;

		ObjectNode jo = jnf.objectNode();

		jo.put("type", "object");

		ObjectNode properties = jo.putObject("properties");
		ArrayNode required = jo.putArray("required");

		Class<?> sup = clazz;

		while (sup.isAnnotationPresent(Entity.class) || sup.isAnnotationPresent(MappedSuperclass.class)) {
			addFields(properties, required, sup);
			sup = sup.getSuperclass();
		}

		return jo;
	}

	private void addFields(ObjectNode properties, ArrayNode required, Class<?> clazz) {
		for (Field f : clazz.getDeclaredFields()) {

			if (f.getName().equals("id"))
				continue;
			if (Modifier.isFinal(f.getModifiers()))
				continue;

			ObjectNode node = properties.putObject(f.getName());

			if (f.getType().isEnum()) {
				node.put("type", "string");
				ArrayNode enumValues = node.putArray("enum");
				for (Object value : f.getType().getEnumConstants()) {
					enumValues.add(value.toString());
				}
			} else {
				String typeName = f.getType().getSimpleName().toLowerCase();
				if (typeName.equals("long") || typeName.equals("short"))
					typeName = "integer";
				if (typeName.equals("double") || typeName.equals("float"))
					typeName = "number";
				node.put("type", typeName);
			}

			handleValidations(f, node);

			if (f.isAnnotationPresent(Pattern.class))
				node.put("pattern", f.getAnnotation(Pattern.class).regexp());

			if (f.isAnnotationPresent(NotNull.class) || f.isAnnotationPresent(NotBlank.class)) {
				required.add(f.getName());
			}

		}
	}

	private void handleValidations(Field f, ObjectNode node) {
		if (f.isAnnotationPresent(Email.class)) {
			node.put("pattern", "^\\S+@\\S+$");
			node.put("type", "string");
			node.put("validationMessage", "Correo invalido");
		}

		if (f.isAnnotationPresent(Min.class)) {
			node.put("minimun", f.getAnnotation(Min.class).value());
		}

		if (f.isAnnotationPresent(Max.class)) {
			node.put("maximum", f.getAnnotation(Max.class).value());
		}

		if (f.isAnnotationPresent(Size.class)) {
			Size size = f.getAnnotation(Size.class);
			node.put("minLength", size.min());
			node.put("maxLength", size.max());
		}
	}

}
