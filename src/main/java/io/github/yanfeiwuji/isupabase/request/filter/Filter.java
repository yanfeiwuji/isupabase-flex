package io.github.yanfeiwuji.isupabase.request.filter;

import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.table.TableInfo;
import io.github.yanfeiwuji.isupabase.request.utils.CacheTableInfoUtils;
import lombok.Data;

import java.util.Arrays;


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
    // remove operate

    private boolean negative = false;
    private String opMark;
    private TokenModifiers modifiers; // any or all or none
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
        String[] split = nextVal.split(StrPool.DOT, 2);
        if (split.length != 2) {
            return;
        }
        // to handler
        this.opMark = split[0];
        this.strValue = split[1];

    }
}
