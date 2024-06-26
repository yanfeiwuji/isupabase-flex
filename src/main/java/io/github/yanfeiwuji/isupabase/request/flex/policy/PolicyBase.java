package io.github.yanfeiwuji.isupabase.request.flex.policy;

import cn.hutool.core.util.ClassUtil;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.mybatis.Mappers;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.table.TableInfoFactory;
import io.github.yanfeiwuji.isupabase.auth.utils.ServletUtils;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstEx;
import io.github.yanfeiwuji.isupabase.request.flex.AuthContext;
import io.github.yanfeiwuji.isupabase.request.flex.TableOneOperateConfig;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExFactory;
import io.github.yanfeiwuji.isupabase.request.flex.TableSetting;
import jakarta.annotation.PostConstruct;
import org.springframework.web.context.request.RequestContextHolder;


/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:38
 */
public abstract class PolicyBase<T> {

    protected QueryCondition EMPTY_CONDITION = QueryCondition.createEmpty();


    abstract TableSetting<T> config();


    protected void deniedOnRow() {
        throw PgrstExFactory.rowSecurityError(ServletUtils.tableName()).get();
    }

    protected PgrstEx deniedOnRowEx() {
        return PgrstExFactory.rowSecurityError(ServletUtils.tableName()).get();
    }


    protected void deniedOnCol() {
        throw PgrstExFactory.columnSecurityError(ServletUtils.tableName()).get();
    }

    protected PgrstEx deniedOnColEx() {
        return PgrstExFactory.columnSecurityError("").get();
    }

}
