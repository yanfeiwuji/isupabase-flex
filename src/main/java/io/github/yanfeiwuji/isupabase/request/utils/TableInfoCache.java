package io.github.yanfeiwuji.isupabase.request.utils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationStartupAware;
import org.springframework.core.metrics.ApplicationStartup;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.NamingBase;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;

import cn.hutool.core.map.MapBuilder;
import cn.hutool.core.map.MapUtil;
import cn.hutool.extra.spring.SpringUtil;

import lombok.experimental.UtilityClass;

/**
 * TableInfoUtils
 */
@UtilityClass
public class TableInfoCache {

    private static final Map<Class<?>, Map<String, String>> CACHE_CLAZZ_PARAM_NAME_COLUMN = new ConcurrentHashMap<>();

    private static final Map<Class<?>, Map<String, ColumnInfo>> CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO = new ConcurrentHashMap<>();
    private static ObjectMapper mapper;

    public void init(ObjectMapper mapper) {
        TableInfoCache.mapper = mapper;
    }

    public Optional<String> realColumn(String paramKey, TableInfo tableInfo) {
        return Optional.ofNullable(CACHE_CLAZZ_PARAM_NAME_COLUMN.computeIfAbsent(tableInfo.getEntityClass(), (it) -> {
            Map<String, String> map = tableInfo.getPropertyColumnMapping();
            return Optional.ofNullable(mapper)
                    .map(ObjectMapper::getPropertyNamingStrategy)
                    .filter(PropertyNamingStrategies.NamingBase.class::isInstance)
                    .map(PropertyNamingStrategies.NamingBase.class::cast)
                    .map(namingBase -> {
                        MapBuilder<String, String> builder = MapUtil.builder();
                        map.forEach((k, v) -> builder.put(namingBase.translate(k), v));
                        return builder.build();
                    }).orElse(map);
        }).get(paramKey));
    }

    public Optional<ColumnInfo> realColumnInfo(String paramKey, TableInfo tableInfo) {
        return Optional.ofNullable(
                CACHE_CLAZZ_PARAM_NAME_COLUMN_INFO.computeIfAbsent(tableInfo.getEntityClass(), (it) -> {
                    List<ColumnInfo> columnInfos = tableInfo.getColumnInfoList();
                    Optional<NamingBase> namingOptional = Optional.ofNullable(mapper)
                            .map(ObjectMapper::getPropertyNamingStrategy)
                            .filter(PropertyNamingStrategies.NamingBase.class::isInstance)
                            .map(PropertyNamingStrategies.NamingBase.class::cast);
                    MapBuilder<String, ColumnInfo> builder = MapUtil.builder();
                    columnInfos.forEach(info -> {
                        builder.put(
                                namingOptional.map(naming -> naming.translate(info.getProperty()))
                                        .orElse(info.getProperty()),
                                info);
                    });
                    return builder.build();
                }).get(paramKey));
    }

}