package io.github.yanfeiwuji.isupabase.config;

import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author yanfeiwuji
 * @date 2024/6/2 15:30
 */
public class PgrstMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter
{
    @Override
    protected void writeInternal(Object object, Type type, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        super.writeInternal(object, type, outputMessage);
    }
}
