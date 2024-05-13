package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import io.github.yanfeiwuji.isupabase.request.utils.ExchangeUtils;
import lombok.Data;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.checkerframework.checker.units.qual.s;

/**
 * only handler column key and value
 * key = not?.op?(mode).value()
 */
@Data
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

    private boolean negative = false;
    private IOperator operator;
    private TokenModifiers modifiers = TokenModifiers.NONE; // any or all or none
    private String strValue;

    public Filter(String paramKey, String paramValue, TableInfo tableInfo) {
        this.paramKey = paramKey;
        this.paramValue = paramValue;
        this.tableInfo = tableInfo;
        this.realProperty = CacheTableInfoUtils.nNRealProperty(paramKey, tableInfo);
        this.realColumn = CacheTableInfoUtils.nNRealColumn(paramKey, tableInfo);

        this.negative = paramValue.startsWith(TokenNegative.NOT.getMark());
        String nextVal = paramValue;
        if (this.negative) {
            nextVal = paramValue.replaceFirst(TokenNegative.NOT.getMark() + StrPool.DOT, "");
        }

        List<String> split = StrUtil.split(nextVal, StrPool.DOT);

        System.out.println(split);
        if (split.size() != 2) {
            return;
        }

        Arrays.asList(TokenModifiers.values())
                .stream().filter(it -> split.get(0).endsWith(it.getMark()))
                .findAny().ifPresent(it -> {
                    this.modifiers = it;
                });

        String opMark = split.get(0).replaceFirst(this.modifiers.getMark(), "");
        System.out.println(opMark);

        // TODO set real op then imple eq logic
        this.operator = TokenQuantOperator.EQ;

        this.strValue = split.get(1);

        System.out.println(split.get(1) + "ddd");
        if (modifiers.equals(TokenModifiers.NONE)) {
            this.value = ExchangeUtils.singleValue(this);
        } else {
            this.quantValue = ExchangeUtils.listValue(this);
        }
    }

    public void handler(QueryWrapper queryWrapper) {
        operator.getHandlerFunc().accept(this, queryWrapper);
    }
}
