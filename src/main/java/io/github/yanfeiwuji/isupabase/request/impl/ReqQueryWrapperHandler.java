package io.github.yanfeiwuji.isupabase.request.impl;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@AllArgsConstructor
@Slf4j
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {

    private final ObjectMapper mapper;
    private final Map<Class<?>, Map<String, String>> cacheClazzNamingColumn = new ConcurrentHashMap<>();
    //
    private final Table<Class<?>, String, String> clazzColumnNaming = new RowKeyTable<>();


    @Override
    public QueryWrapper handler(ServerRequest request, TableInfo tableInfo) {
        MultiValueMap<String, String> params = request.params();
        QueryWrapper wrapper = QueryWrapper.create();
        handlerHorizontalFilter(params, tableInfo, wrapper);
        return wrapper;
    }

    public QueryWrapper handlerHorizontalFilter(MultiValueMap<String, String> params, TableInfo tableInfo, QueryWrapper queryWrapper) {
        params.forEach((key, value) -> {
            handlerSimple(key, value.getFirst(), tableInfo, queryWrapper);
        });
        return queryWrapper;
    }

    /**
     * simple
     * id=eq.name
     *
     * @param key
     * @param value
     * @param tableInfo
     * @param queryWrapper
     * @return
     */
    public QueryWrapper handlerSimple(String key, String value, TableInfo tableInfo, QueryWrapper queryWrapper) {
        String[] split = value.split("\\.", 1);
        // queryWrapper
        String[] allColumns = tableInfo.getAllColumns();

        Arrays.stream(allColumns).forEach(System.out::println);
        log.info("colums:{}", allColumns);
        Map<String, String> propertyColumnMapping = tableInfo.getPropertyColumnMapping();

        return queryWrapper;
    }

    private String as(String paramKey, TableInfo tableInfo) {


        return cacheClazzNamingColumn.computeIfAbsent(tableInfo.getEntityClass(), (it) -> {
            Map<String, String> map = tableInfo.getPropertyColumnMapping();
            PropertyNamingStrategy propertyNamingStrategy = mapper.getPropertyNamingStrategy();
            return Optional.ofNullable(propertyNamingStrategy)
                    .filter(PropertyNamingStrategies.NamingBase.class::isInstance)
                    .map(PropertyNamingStrategies.NamingBase.class::cast)
                    .map(namingBase -> {
                        MapBuilder<String, String> builder = MapUtil.builder();
                        map.forEach((k, v) -> builder.put(namingBase.translate(k), v));
                        return builder.build();
                    }).orElse(Map.of());
        }).get(paramKey);

    }


}
