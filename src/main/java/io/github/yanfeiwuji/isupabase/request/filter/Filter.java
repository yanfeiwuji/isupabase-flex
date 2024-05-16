package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.text.CharSequenceUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.query.QueryColumn;
import com.mybatisflex.core.query.QueryCondition;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import io.github.yanfeiwuji.isupabase.request.ex.MReqExManagers;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import io.github.yanfeiwuji.isupabase.request.utils.OperationUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
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

            this.filters = MTokens.splitByComma(need)
                    .stream()
                    .map(it -> MTokens.LOGIC_KV.keyValue(it).orElse(MTokens.DOT.keyValue(it)
                            .orElseThrow(MReqExManagers.FAILED_TO_PARSE.supplierReqEx(it))))
                    .map(it -> {
                        log.info("kv:{}", it);
                        return it;
                    })
                    .map(it -> new Filter(it.key(), it.value(), tableInfo))
                    .toList();
        } else {
            handlerSingle();
        }
    }

    private void handlerSingle() {
        this.realProperty = CacheTableInfoUtils.nNRealProperty(paramKey, tableInfo);
        this.realColumn = CacheTableInfoUtils.nNRealColumn(paramKey, tableInfo);
        this.queryColumn = CacheTableInfoUtils.nNRealQueryColumn(paramKey, tableInfo);
        log.info("rp:{},rc:{}", realProperty, realColumn);
        this.negative = MTokens.NOT.find(paramValue);
        String nextVal = paramValue;

        if (this.negative) {
            nextVal = MTokens.NOT.value(paramValue).orElse(paramValue);
        }
        log.info("nextVal:{}", nextVal);
        this.operator = MTokens.DOT.first(nextVal)
                .flatMap(OperationUtils::markToOperator)
                .orElseThrow(MReqExManagers.FAILED_TO_PARSE.supplierReqEx(paramValue));

        if (OperationUtils.isQuantOperator(operator)) {
            this.modifier = operator.first(nextVal)
                    .filter(it -> !it.isBlank())
                    .map(EModifier::valueOf)
                    .orElse(EModifier.none);
        }

        this.strValue = this.operator.value(nextVal).orElse("");
        log.info("mark:{}", this.operator.mark());
        log.info("this.strValue:{}", this.strValue);
        this.initValue();
    }

    private void initValue() {
        try {
            if (OperationUtils.isInOperator(this.operator)) {
                this.quantValue = ExchangeUtils.parenthesesWrapListValue(this);
                return;
            }
            if (OperationUtils.isIsOperator(this.operator)) {
                if (!OperationUtils.isIsValue(this.strValue)) {
                    throw MReqExManagers.FAILED_TO_PARSE.reqEx(this.paramValue);
                }

                if (OperationUtils.isIsBoolValue(this.strValue)) {
                    if (!CacheTableInfoUtils.nNRealColumnInfo(paramKey,
                            tableInfo).getPropertyType()
                            .equals(Boolean.class)) {
                        // TODO get real erro
                        throw MReqExManagers.FAILED_TO_PARSE.reqEx(this.paramValue);
                    }
                }
                return;
            }

            if (modifier.equals(EModifier.none)) {
                this.value = ExchangeUtils.singleValue(this);
            } else {
                this.quantValue = ExchangeUtils.delimWrapListValue(this);
            }
        } catch (JsonProcessingException e) {
            log.info(paramValue);
            ColumnInfo columnInfo = CacheTableInfoUtils.nNRealColumnInfo(paramKey, tableInfo);
            throw MDbExManagers.INVALID_INPUT
                    .reqEx(columnInfo.getPropertyType().getSimpleName(), strValue);
        }
    }

    public QueryCondition toQueryCondition() {
        return this.operator.handler().apply(this);
    }

}
