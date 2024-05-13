package io.github.yanfeiwuji.isupabase.request.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;

import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Map;
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

    public QueryWrapper handlerHorizontalFilter(MultiValueMap<String, String> params, TableInfo tableInfo,
                                                QueryWrapper queryWrapper) {
        params.forEach((key, value) -> {
            try {
                handlerSimple(key, value.getFirst(), tableInfo, queryWrapper);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
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

    public QueryWrapper handlerSimple(String key, String value, TableInfo tableInfo, QueryWrapper queryWrapper) throws JsonProcessingException {

        String column = CacheTableInfoUtils.nNRealColumn(key, tableInfo);
        String property = CacheTableInfoUtils.nNRealProperty(key, tableInfo);

        String json = new JSONObject().set(key, value).toString();
        Object o = mapper.readValue(json, tableInfo.getEntityClass());
        Object propertyValue = BeanUtil.getProperty(o, property);

        log.info("o: {} key:{}，val-p:{} column:{} propertyValue:{}", o, key, property, column, propertyValue);
        queryWrapper.eq(column, propertyValue);


        return queryWrapper;
    }

}
