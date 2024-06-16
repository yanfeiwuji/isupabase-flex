package io.github.yanfeiwuji.isupabase.config;

import cn.hutool.core.util.ClassUtil;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.flex.ITableOneOperate;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfigFor;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.experimental.UtilityClass;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 18:38
 */
@UtilityClass
public class TableConfigUtils {
    public List<TableOneOperateConfigFor> load(ApplicationContext context) {
        List<TableOneOperateConfigFor> configs = new ArrayList<>();
        final Map<String, ITableOneOperate> beansOfType = context.getBeansOfType(ITableOneOperate.class);

        beansOfType.forEach((k, v) -> {

            final Class<?> typeArgument = ClassUtil.getTypeArgument(v.getClass());
            final TableInfo tableInfo = TableInfoFactory.ofEntityClass(typeArgument);
            final String tableName = tableInfo.getTableName();
            final TableOneOperateConfigFor tableOneOperateConfigFor = new TableOneOperateConfigFor(tableName, OperateType.SELECT, v.toConfig());
            configs.add(tableOneOperateConfigFor);

        });

        return configs;
    }
}
