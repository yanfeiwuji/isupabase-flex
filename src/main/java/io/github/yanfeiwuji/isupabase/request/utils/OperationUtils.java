package io.github.yanfeiwuji.isupabase.request.utils;

import cn.hutool.core.map.MapUtil;
import io.github.yanfeiwuji.isupabase.request.ex.MReqExManagers;
import io.github.yanfeiwuji.isupabase.request.filter.*;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class OperationUtils {
    private static final Map<String, IOperator> MARK_OPERATOR;
    private static final Map<String, TokenLogicOperator> MARL_LOGIC_OPERATOR;

    static {
        MARK_OPERATOR = MapUtil.newConcurrentHashMap();
        Arrays.stream(TokenSimpleOperator.values())
                .forEach(OperationUtils::putMarkOperator);
        Arrays.stream(TokenQuantOperator.values())
                .forEach(OperationUtils::putMarkOperator);
        Arrays.stream(TokenInOperator.values())
                .forEach(OperationUtils::putMarkOperator);
        MARL_LOGIC_OPERATOR = MapUtil.newConcurrentHashMap();

        Arrays.stream(TokenLogicOperator.values())
                .forEach(OperationUtils::putMarkLogicOperator);
    }

    private void putMarkOperator(IOperator operator) {
        MARK_OPERATOR.put(operator.getMark(), operator);
    }

    private void putMarkLogicOperator(TokenLogicOperator operator) {
        MARL_LOGIC_OPERATOR.put(operator.getMark(), operator);
    }

    public Optional<IOperator> markToOperator(String mark) {
        return Optional.ofNullable(MARK_OPERATOR.get(mark));
    }

    public Optional<TokenLogicOperator> markToLogicOperator(String mark) {
        return Optional.ofNullable(MARL_LOGIC_OPERATOR.get(mark));
    }

}
