package io.github.yanfeiwuji.isupabase.config;

import com.mybatisflex.core.dialect.KeywordWrap;
import com.mybatisflex.core.dialect.LimitOffsetProcessor;
import com.mybatisflex.core.dialect.OperateType;
import com.mybatisflex.core.dialect.impl.CommonsDialectImpl;
import com.mybatisflex.core.query.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class AuthDialectImpl extends CommonsDialectImpl {
    private static Map<String, Map<OperateType, Supplier<QueryCondition>>> rlsPolicyMap = Map.of();

    public AuthDialectImpl(KeywordWrap keywordWrap, LimitOffsetProcessor limitOffsetProcessor) {
        super(keywordWrap, limitOffsetProcessor);
    }

    @Override
    public void prepareAuth(QueryWrapper queryWrapper, OperateType operateType) {

        final QueryCondition whereQueryCondition = CPI.getWhereQueryCondition(queryWrapper);
        applyRls(queryWrapper, operateType);
        conditions(whereQueryCondition, new ArrayList<>()).forEach(it -> {
            final QueryWrapper qw = it.getQueryWrapper();
            applyRls(qw, operateType);
        });
        super.prepareAuth(queryWrapper, operateType);
    }

    public List<OperatorSelectCondition> conditions(QueryCondition queryCondition, List<OperatorSelectCondition> selectConditions) {

        if (queryCondition instanceof OperatorSelectCondition selectCondition) {
            selectConditions.add(selectCondition);
        }
        final QueryCondition nextCondition = CPI.getNextCondition(queryCondition);
        if (Objects.nonNull(nextCondition)) {
            conditions(nextCondition, selectConditions);
        }
        return selectConditions;
    }


    private void applyRls(QueryWrapper queryWrapper, OperateType operateType) {

        List<QueryTable> queryTables = CPI.getQueryTables(queryWrapper);

        queryTables.forEach(it ->
                Optional.ofNullable(rlsPolicyMap).map(map -> map.get(it.getName()))
                        .map(map -> map.get(operateType))
                        .map(Supplier::get)
                        .ifPresent(queryWrapper::and)
        );
    }


    public static synchronized void loadRls(List<RlsPolicy> rlsPolicies) {
        AuthDialectImpl.rlsPolicyMap = rlsPolicies.stream().collect(Collectors.groupingBy(RlsPolicy::tableName,
                Collectors.mapping(it -> it,
                        Collectors.toMap(RlsPolicy::operateType, RlsPolicy::queryCondition)
                )
        ));
    }

}
