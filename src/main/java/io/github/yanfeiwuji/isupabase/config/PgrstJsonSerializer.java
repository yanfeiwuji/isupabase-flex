package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;

@AllArgsConstructor
@Slf4j
public class PgrstJsonSerializer extends JsonSerializer<Object> {
    private final ObjectMapper mapper;


    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {

        Class<?> aClass = value.getClass();
        gen.writeStartObject();
        gen.writeStringField("class", aClass.getName());
        Arrays.stream(aClass.getFields()).forEach(f -> {
            f.setAccessible(true);
            try {
                Object o = f.get(value);
                System.out.println(o);
                if (o == null) {
                    gen.writeNullField(f.getName());
                } else if (o instanceof String) {
                    gen.writeStringField(f.getName(), (String) o);
                } else if (o instanceof Integer) {
                    gen.writeNumberField(f.getName(), (Integer) o);
                } else if (o instanceof Long) {
                    gen.writeNumberField(f.getName(), (Long) o);
                } else if (o instanceof Float) {
                    gen.writeNumberField(f.getName(), (Float) o);
                } else if (o instanceof Double) {
                    gen.writeNumberField(f.getName(), (Double) o);
                } else if (o instanceof Boolean) {
                    gen.writeBooleanField(f.getName(), (Boolean) o);
                } else if (o instanceof byte[]) {
                    gen.writeBinaryField(f.getName(), (byte[]) o);
                } else {
                    gen.writeObjectField(f.getName(), o);
                }
            } catch (IllegalAccessException e) {

                log.info("error :{} ", e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });


        gen.writeEndObject();
    }
}
