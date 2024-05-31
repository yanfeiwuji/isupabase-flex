package io.github.yanfeiwuji.isupabase.config;

import cn.hutool.core.bean.BeanUtil;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@AllArgsConstructor
@Slf4j
public class PgrstJsonSerializer extends JsonSerializer<Object> {
    private final ObjectMapper mapper;


    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        boolean notHandler = Math.random() > 0.5;
        if (notHandler) {
            gen.writeRaw(mapper.writeValueAsString(value));
        } else {
            gen.writeStartObject();

            gen.writeStringField("a", value.toString());
            gen.writeEndObject();

        }
    }
}
