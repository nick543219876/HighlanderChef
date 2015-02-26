package com.highlanderchef;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

public class DirectionSerializer extends JsonSerializer<Direction> {
	@Override
	public void serialize(Direction value, JsonGenerator jgen, SerializerProvider provider)
			throws IOException, JsonProcessingException {
		jgen.writeStartObject();
		jgen.writeStringField("text", value.text);
		jgen.writeArrayFieldStart("img");
		Comm c = new Comm();
		for (int i = 0; i < value.images.size(); i++) {
			System.out.println("DirectionSerializer adding image " + i);
			jgen.writeString(c.imageUpload(value.images.get(i)));
		}
		jgen.writeEndArray();
		jgen.writeEndObject();
	}
}