package io.github.yanfeiwuji.isupabase.request.utils;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.table.TableInfo;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author yanfeiwuji
 * @date 2024/6/5 16:41
 */
@UtilityClass
public class FlexUtils {
    public void insertOrUpdateSelective(BaseMapper<Object> baseMapper, List<Object> list, TableInfo tableInfo) {
        final Map<Boolean, List<Object>> collect = list.stream().collect(Collectors.groupingBy(it -> {
            Object[] pkArgs = tableInfo.buildPkSqlArgs(it);
            return pkArgs.length == 0 || pkArgs[0] == null;
        }));
        Optional.ofNullable(collect.get(true)).ifPresent(baseMapper::insertBatch);
        Optional.ofNullable(collect.get(false)).ifPresent(it -> it.forEach(baseMapper::update));
    }
}
