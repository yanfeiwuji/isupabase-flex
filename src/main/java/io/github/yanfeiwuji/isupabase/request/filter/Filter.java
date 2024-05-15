package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.ColumnInfo;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.ex.MDbExManagers;
import io.github.yanfeiwuji.isupabase.request.ex.MReqExManagers;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import io.github.yanfeiwuji.isupabase.request.utils.OperationUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

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

    // real value
    private Object value;
    // quant in here
    private List<Object> quantValue;
    // remove operate

    private boolean negative;

    private Operator operator;

    // logic use
    private List<Filter> filters;

    private Modifier modifier = Modifier.none; // any or all or none
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

            this.filters = CharSequenceUtil.split(need, ',')
                    .stream()
                    .map(MTokens.DOT::keyValue)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .map(it -> new Filter(it.key(), it.value(), tableInfo))
                    .toList();
        } else {
            handlerSingle();
        }

    }

    private void handlerSingle() {
        this.realProperty = CacheTableInfoUtils.nNRealProperty(paramKey, tableInfo);
        this.realColumn = CacheTableInfoUtils.nNRealColumn(paramKey, tableInfo);

        log.info("rp:{},rc:{}", realProperty, realColumn);
        this.negative = MTokens.NOT.find(paramValue);
        String nextVal = paramValue;

        if (this.negative) {
            nextVal = MTokens.NOT.value(paramValue).orElse(paramValue);
        }
        log.info("nextVal：{}", nextVal);
        this.operator = MTokens.DOT.first(nextVal)
                .flatMap(OperationUtils::markToOperator)
                .orElseThrow(MReqExManagers.FAILED_TO_PARSE.supplierReqEx(paramValue));

        if (OperationUtils.isQuantOperator(operator)) {
            this.modifier = operator.first(nextVal)
                    .filter(it -> !it.isBlank())
                    .map(Modifier::valueOf)
                    .orElse(Modifier.none);
        }

        this.strValue = this.operator.value(nextVal).orElse("");
        log.info("this.strValue:{}", this.strValue);
        this.initValue();
    }

    private void initValue() {
        try {
            if (OperationUtils.isInOperator(this.operator)) {
                this.quantValue = ExchangeUtils.parenthesesWrapListValue(this);
                return;
            }

            if (modifier.equals(Modifier.none)) {
                this.value = ExchangeUtils.singleValue(this);
            } else {
                this.quantValue = ExchangeUtils.delimWrapListValue(this);
            }
        } catch (JsonProcessingException e) {
            ColumnInfo columnInfo = CacheTableInfoUtils.nNRealColumnInfo(paramKey, tableInfo);
            throw MDbExManagers.INVALID_INPUT.reqEx(columnInfo.getPropertyType().getSimpleName(), strValue);
        }
    }

    public void handler(QueryWrapper queryWrapper) {
        operator.handler().accept(this, queryWrapper);
    }

}
