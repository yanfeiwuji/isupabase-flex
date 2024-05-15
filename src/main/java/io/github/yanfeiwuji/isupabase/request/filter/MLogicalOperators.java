package io.github.yanfeiwuji.isupabase.request.filter;

import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.entity.table.SysUserTableDef;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.regex.Pattern;

@UtilityClass
public class MLogicalOperators {
    public final Operator AND = new Operator("and", Pattern.compile("^and(.*)"), (f, q) -> {

        Consumer<QueryWrapper> consumer = qw -> f.getFilters().forEach(
                it -> it.handler(qw));
        q.and(consumer);
    });
    public final Operator OR = new Operator("or", Pattern.compile("^or(.*)"), (f, q) -> {
        Consumer<QueryWrapper> consumer = qw -> f.getFilters().forEach(
                it -> it.handler(qw));
        q.and(consumer);
        SysUserTableDef.SYS_USER.AGE.eq("");

        f.getFilters();
    });
}
