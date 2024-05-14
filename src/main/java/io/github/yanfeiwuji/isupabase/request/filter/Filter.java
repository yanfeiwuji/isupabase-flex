package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.CharUtil;
import cn.hutool.core.util.StrUtil;

import cn.hutool.json.JSONUtil;
import com.fasterxml.jackson.core.JsonParseException;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.checkerframework.checker.units.qual.s;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * only handler column key and value
 * key = not?.op?(mode).value()
 */
@Data
public class Filter {
    private static final Logger log = LoggerFactory.getLogger(Filter.class);
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

    private IOperator operator;

    // logic use
    private List<Filter> filters;

    private TokenModifiers modifiers = TokenModifiers.NONE; // any or all or none
    private String strValue;

    public Filter(String paramKey, String paramValue, TableInfo tableInfo) {
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.tableInfo = tableInfo;



        this.realProperty = CacheTableInfoUtils.nNRealProperty(paramKey, tableInfo);
        this.realColumn = CacheTableInfoUtils.nNRealColumn(paramKey, tableInfo);

        log.info("rp:{},rc:{}", realProperty, realColumn);
        this.negative = TokenNegative.paramKeyIsNegative(paramKey);
        String nextVal = paramValue;

        if (this.negative) {
            nextVal = TokenNegative.removeNotDot(paramKey);
        }
        log.info(nextVal);
        //


        if (!CharSequenceUtil.contains(nextVal, StrPool.DOT)) {
            throw MReqExManagers.FAILED_TO_PARSE.reqEx(paramValue);
        }
        List<String> split = CharSequenceUtil.split(nextVal, CharPool.DOT, 2);

        Arrays.stream(TokenModifiers.values())
                .filter(it -> split.getFirst().endsWith(it.getMark()))
                .findAny().ifPresent(this::setModifiers);

        String opMark =
                CharSequenceUtil.replace(split.getFirst(), this.modifiers.getMark(), "");

        log.info("mark:{}", opMark);
        this.operator = OperationUtils.markToOperator(opMark)
                .orElseThrow(MReqExManagers.FAILED_TO_PARSE.supplierReqEx(paramValue));

        log.info("op mark :{}", opMark);

        this.strValue = split.get(1);
        log.info("this.strValue:{}", this.strValue);
        this.initValue();

    }

    private void initValue() {
        try {
            if (this.operator.equals(TokenInOperator.IN)) {
                this.quantValue = ExchangeUtils.parenthesesWrapListValue(this);
                return;
            }

            if (modifiers.equals(TokenModifiers.NONE)) {
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
        operator.getHandlerFunc().accept(this, queryWrapper);
    }


}
