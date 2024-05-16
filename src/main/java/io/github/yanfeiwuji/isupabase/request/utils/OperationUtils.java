package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.map.MapUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.request.filter.*;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@UtilityClass
@Slf4j
public class OperationUtils {
    private static final Map<String, Operator> MARK_OPERATORS;
    private static final Map<String, String> LOGIC_MARKS = Stream.of("and", "or")
            .collect(Collectors.toMap(it -> it, it -> it));

    private static final Map<String, String> QUANT_MARKS = Stream
            .of("eq", "gte", "gt", "lte", "lt", "like", "ilike", "match", "imatch")
            .collect(Collectors.toMap(it -> it, it -> it));

    private static final Map<String, String> IS_VALUES = Stream.of("false", "true", "null", "unknown")
            .collect(Collectors.toMap(it -> it, it -> it));

    private static final Map<String, String> IS_BOOL_VALUES = Stream.of("false", "true")
            .collect(Collectors.toMap(it -> it, it -> it));

    static {
        MARK_OPERATORS = MapUtil.newConcurrentHashMap();
        OperationUtils.putMarkOperator(MLogicalOperators.AND);
        OperationUtils.putMarkOperator(MLogicalOperators.OR);

        OperationUtils.putMarkOperator(MInIsOperators.IN);
        OperationUtils.putMarkOperator(MInIsOperators.IS);

        OperationUtils.putMarkOperator(MQuantOperators.EQ);
        OperationUtils.putMarkOperator(MQuantOperators.GTE);
        OperationUtils.putMarkOperator(MQuantOperators.GT);
        OperationUtils.putMarkOperator(MQuantOperators.LTE);
        OperationUtils.putMarkOperator(MQuantOperators.LT);
        OperationUtils.putMarkOperator(MQuantOperators.LIKE);
        OperationUtils.putMarkOperator(MQuantOperators.ILIKE);
        OperationUtils.putMarkOperator(MQuantOperators.MATCH);
        OperationUtils.putMarkOperator(MQuantOperators.IMATCH);
        OperationUtils.putMarkOperator(MQuantOperators.IMATCH);

        OperationUtils.putMarkOperator(MSimpleOperators.NEQ);
        OperationUtils.putMarkOperator(MSimpleOperators.CS);
        OperationUtils.putMarkOperator(MSimpleOperators.CD);
        OperationUtils.putMarkOperator(MSimpleOperators.OV);
        OperationUtils.putMarkOperator(MSimpleOperators.SL);
        OperationUtils.putMarkOperator(MSimpleOperators.SR);
        OperationUtils.putMarkOperator(MSimpleOperators.NXL);
        OperationUtils.putMarkOperator(MSimpleOperators.NXR);
        OperationUtils.putMarkOperator(MSimpleOperators.ADJ);

    }

    private void putMarkOperator(Operator operator) {
        MARK_OPERATORS.put(operator.mark(), operator);
    }

    public Optional<Operator> markToOperator(String mark) {
        return Optional.ofNullable(MARK_OPERATORS.get(mark));
    }

    public boolean isLogicOperator(Operator operator) {
        return LOGIC_MARKS.containsKey(operator.mark());
    }

    public boolean isInOperator(Operator operator) {
        return operator.mark().equals(MInIsOperators.IN.mark());
    }

    public boolean isIsOperator(Operator operator) {
        return operator.mark().equals(MInIsOperators.IS.mark());
    }

    public boolean isIsValue(String value) {
        return IS_VALUES.containsKey(value);
    }

    public boolean isIsBoolValue(String value) {
        return IS_BOOL_VALUES.containsKey(value);
    }

    public boolean isQuantOperator(Operator operator) {
        return QUANT_MARKS.containsKey(operator.mark());
    }

}
