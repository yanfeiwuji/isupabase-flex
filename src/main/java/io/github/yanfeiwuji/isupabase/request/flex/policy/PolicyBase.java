package io.github.yanfeiwuji.isupabase.request.flex.policy;

import cn.hutool.core.util.ClassUtil;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.request.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.TableOneOperateConfig;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.flex.TableSetting;
import jakarta.annotation.PostConstruct;


/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public abstract class PolicyBase<T> {

    protected QueryCondition EMPTY_CONDITION = QueryCondition.createEmpty();
    protected BaseMapper<T> baseMapper;
    protected String tableName;

    abstract TableSetting<T> config();

    @PostConstruct
    protected void init() {
        final Class<T> typeArgument = (Class<T>) ClassUtil.getTypeArgument(this.getClass());
        final TableInfo tableInfo = TableInfoFactory.ofEntityClass(typeArgument);
        baseMapper = Mappers.ofEntityClass(typeArgument);
        tableName = tableInfo.getTableName();
    }

    protected void rowDenied() {
        throw PgrstExFactory.rowSecurityError(tableName).get();
    }
}
