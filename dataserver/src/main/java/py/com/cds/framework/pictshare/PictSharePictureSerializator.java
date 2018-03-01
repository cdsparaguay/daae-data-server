package py.com.cds.framework.pictshare;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class PictSharePictureSerializator extends JsonSerializer<String> {

	@Override
	public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
			throws IOException, JsonProcessingException {
		if (StringUtils.isEmpty(value))
			gen.writeString("");
		else if (value.startsWith("http"))
			gen.writeString(value);
		else
			gen.writeString(PictShareHelper.getFullUrl(value));
	}

}
