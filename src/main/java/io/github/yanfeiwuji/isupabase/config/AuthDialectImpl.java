package io.github.yanfeiwuji.isupabase.config;

import com.mybatisflex.core.dialect.KeywordWrap;
import com.mybatisflex.core.dialect.LimitOffsetProcessor;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.CPI;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.query.QueryTable;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import com.mybatisflex.core.util.MapperUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AuthDialectImpl extends CommonsDialectImpl {

    public AuthDialectImpl() {
        super(KeywordWrap.DOUBLE_QUOTATION, LimitOffsetProcessor.POSTGRESQL);
    }

    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {
        log.info("qw");
        List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);
        if (queryTables == null || queryTables.isEmpty()) {
            return;
        }
        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        final boolean b = CPI.containsTable(whereQueryCondition, "sys_user_ext");

        System.out.println(b + "<has> sys_user_ext"+"<"+operateType+">");
        for (QueryTable queryTable : queryTables) {
            System.out.println(queryTable + "--");
        }

        super.prepareAuth(queryWrapper, operateType);
    }


    @Override
    public void prepareAuth(String schema, String tableName, StringBuilder sql, OperateType operateType) {
        log.info("sql");
        super.prepareAuth(schema, tableName, sql, operateType);
    }

    @Override
    public void prepareAuth(TableInfo tableInfo, StringBuilder sql, OperateType operateType) {
        log.info("sql");
        super.prepareAuth(tableInfo, sql, operateType);
    }
}
