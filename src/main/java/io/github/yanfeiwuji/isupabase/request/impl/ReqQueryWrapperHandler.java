package io.github.yanfeiwuji.isupabase.request.impl;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.multi.RowKeyTable;
import cn.hutool.core.map.multi.Table;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;

import ch.qos.logback.core.joran.util.beans.BeanUtil;
import io.github.yanfeiwuji.isupabase.request.IReqQueryWrapperHandler;
import io.github.yanfeiwuji.isupabase.request.ex.DbExManagers;
import io.github.yanfeiwuji.isupabase.request.utils.TableInfoCache;
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

    public QueryWrapper handlerHorizontalFilter(MultiValueMap<String, String> params, TableInfo tableInfo,
            QueryWrapper queryWrapper) {
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
        TableInfoCache.realColumn(key, tableInfo).orElseThrow();

        ColumnInfo columnInfo = TableInfoCache.realColumnInfo(key, tableInfo)
                .orElseThrow(DbExManagers.COLUMN_NOT_FOUND.supplierReqEx(key));
        queryWrapper.eq(columnInfo.getColumn(), Integer.valueOf(value));
        // TODO next from here
        tableInfo.getColumnInfoList();
        return queryWrapper;
    }

}
