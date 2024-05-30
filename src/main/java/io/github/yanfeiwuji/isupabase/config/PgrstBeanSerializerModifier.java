package io.github.yanfeiwuji.isupabase.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class PgrstBeanSerializerModifier extends BeanSerializerModifier {


    @Override
    public List<BeanPropertyWriter> orderProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        System.out.println("herssdse");
        beanProperties.stream().map(it -> it.getName());
        return beanProperties;
    }

    @Override
    public List<BeanPropertyWriter>  changeProperties(SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
        System.out.println("herssdse");
        beanProperties.stream().map(it -> it.getName()).forEach(System.out::println);
        return beanProperties;
    }
}
