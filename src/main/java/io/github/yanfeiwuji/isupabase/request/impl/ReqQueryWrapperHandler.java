package io.github.yanfeiwuji.isupabase.request.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.servlet.function.ServerRequest;

@Component
@AllArgsConstructor
public class ReqQueryWrapperHandler implements IReqQueryWrapperHandler {

    private final ObjectMapper mapper;
    private final PropertyNamingStrategies.NamingBase namingBase;

    @Override
    public QueryWrapper handler(ServerRequest request, TableInfo tableInfo) {
        MultiValueMap<String, String> params = request.params();
        QueryWrapper wrapper = QueryWrapper.create();

        return wrapper;
    }

    public QueryWrapper handlerHorizontalFilter(MultiValueMap<String, String> params, TableInfo tableInfo, QueryWrapper queryWrapper) {
        params.forEach((key, value) -> {

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

        tableInfo.getColumnByProperty(key);
        // namingBase.translate();

        return queryWrapper;
    }
}
