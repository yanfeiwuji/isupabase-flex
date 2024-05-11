package io.github.yanfeiwuji.isupabase.request.impl;

import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.json.JSONUtil;
import io.github.yanfeiwuji.isupabase.request.BodyInfo;
import io.github.yanfeiwuji.isupabase.request.IBodyHandler;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class BodyHandler implements IBodyHandler {
    private final ObjectMapper mapper;

    @Override
    @SneakyThrows
    public <T> BodyInfo<T> handler(ServerRequest request, Class<T> entityClass) {
        String body = request.body(String.class);
        if (JSONUtil.isTypeJSONArray(body)) {
            JavaType listType = mapper.getTypeFactory().constructParametricType(List.class, entityClass);
            List<T> list = mapper.readValue(body, listType);
            return new BodyInfo<>(list);
        } else {
            T value = mapper.readValue(body, entityClass);
            return new BodyInfo<>(value);
        }

        // handler many and return value
    }

}
