package io.github.yanfeiwuji.isupabase.flex.policy;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ClassUtil;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.flex.TableOneOperateConfig;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import jakarta.annotation.PostConstruct;


/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public abstract class PolicyBase<C extends AuthContext, T> {

    protected QueryCondition EMPTY_CONDITION = QueryCondition.createEmpty();

    protected String tableName;

    abstract TableOneOperateConfig<C, T> config();

    @PostConstruct
    protected void init() {

        final Class<?> typeArgument = ClassUtil.getTypeArgument(this.getClass(), 1);
        final TableInfo tableInfo = TableInfoFactory.ofEntityClass(typeArgument);
        tableName = tableInfo.getTableName();
    }

    protected void rowDenied() {
        throw PgrstExFactory.rowSecurityError(tableName).get();
    }
}
