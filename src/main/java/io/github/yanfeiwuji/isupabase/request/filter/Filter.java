package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.text.CharSequenceUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import io.github.yanfeiwuji.isupabase.request.ex.MReqExManagers;
import io.github.yanfeiwuji.isupabase.request.token.MTokens;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import io.github.yanfeiwuji.isupabase.request.utils.OperationUtils;
import io.github.yanfeiwuji.isupabase.request.utils.TokenUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * only handler column key and value
 * key = not?.op?(mode).value()
 */
@Data
@Slf4j
public class Filter {

    private String paramKey;
    private String paramValue;
    private TableInfo tableInfo;

    private String realProperty;
    private String realColumn;

    private QueryColumn queryColumn;

    // real value
    private Object value;
    // quant in here
    private List<Object> quantValue;
    // remove operate

    private boolean negative;

    private Operator operator;

    // logic use
    private List<Filter> filters;

    private EModifier modifier = EModifier.none; // any or all or none
    private String strValue;

    public Filter(String paramKey, String paramValue, TableInfo tableInfo) {
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.tableInfo = tableInfo;
        // not.or or not.and
        this.negative = MTokens.NOT.find(paramKey);

        String nextKey = MTokens.NOT.value(paramKey).orElse(paramKey);

        Optional<Operator> logicOp = OperationUtils.markToOperator(nextKey)
                .filter(OperationUtils::isLogicOperator);

        if (logicOp.isPresent()) {
            this.operator = logicOp.get();
            String need = CharSequenceUtil.strip(paramValue, "(", ")");

            this.filters = TokenUtils.splitByComma(need)
                    .stream()

                    .map(it -> MTokens.LOGIC_KV.keyValue(it).orElse(MTokens.DOT.keyValue(it)
                            .orElseThrow(MReqExManagers.FAILED_TO_PARSE.supplierReqEx(it))))

                    .map(it -> new Filter(it.key(), it.value(), this.tableInfo))
                    .toList();
        } else {
            handlerSingle();
        }
    }

    private void handlerSingle() {
        realProperty = CacheTableInfoUtils.nNRealProperty(paramKey, tableInfo);
        realColumn = CacheTableInfoUtils.nNRealColumn(paramKey, tableInfo);
        queryColumn = CacheTableInfoUtils.nNRealQueryColumn(paramKey, tableInfo);
        log.info("rp:{},rc:{}", realProperty, realColumn);
        negative = MTokens.NOT.find(paramValue);
        String nextVal = paramValue;

        if (negative) {
            nextVal = MTokens.NOT.value(paramValue).orElse(paramValue);
        }
        log.info("nextVal:{}", nextVal);
        operator = MTokens.DOT.first(nextVal)
                .flatMap(OperationUtils::markToOperator)
                .orElseThrow(MReqExManagers.FAILED_TO_PARSE.supplierReqEx(paramValue));

        if (OperationUtils.isQuantOperator(operator)) {
            modifier = operator.first(nextVal)
                    .filter(it -> !it.isBlank())
                    .map(EModifier::valueOf)
                    .orElse(EModifier.none);
        }

        strValue = operator.value(nextVal).orElse("");
        log.info("mark:{}", operator.mark());
        log.info("strValue:{}", strValue);
        initValue();
    }

    private void initValue() {
        try {
            if (OperationUtils.isInOperator(operator)) {
                handlerIn();
            } else if (OperationUtils.isIsOperator(operator)) {
                handlerIs();
            } else {
                if (Objects.requireNonNull(modifier) == EModifier.none) {
                    value = ExchangeUtils.singleValue(this);
                } else {
                    quantValue = ExchangeUtils.delimWrapListValue(this);
                }
            }
        } catch (JsonProcessingException e) {
            log.info(paramValue);
            ColumnInfo columnInfo = CacheTableInfoUtils.nNRealColumnInfo(paramKey, tableInfo);
            throw MDbExManagers.INVALID_INPUT
                    .reqEx(columnInfo.getPropertyType().getSimpleName(), strValue);
        }
    }

    private void handlerIn() throws JsonProcessingException {
        quantValue = ExchangeUtils.parenthesesWrapListValue(this);
    }

    private void handlerIs() throws JsonProcessingException {
        if (!OperationUtils.isIsValue(strValue)) {
            throw MReqExManagers.FAILED_TO_PARSE.reqEx(paramValue);
        }

        if (OperationUtils.isIsBoolValue(strValue) &&
                !CacheTableInfoUtils.nNRealColumnInfo(paramKey, tableInfo).getPropertyType()
                        .equals(Boolean.class)) {
            throw MDbExManagers.DATATYPE_MISMATCH.reqEx("IS %s %s".formatted(
                    negative ? "NOT" : "", strValue),
                    "boolean",
                    CacheTableInfoUtils.realDbType(paramKey, tableInfo));
        }

    }

    public QueryCondition toQueryCondition() {
        return operator.handler().apply(this);
    }

}
