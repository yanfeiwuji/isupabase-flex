package io.github.yanfeiwuji.isupabase.stroage.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/20 17:37
 */
public  record ObjectListParam(Long limit, Long offset, String prefix,
                               @JsonProperty("sortBy") List<ObjectListParamOrder> sortBy) {

    /// todo gen querywrapper
    public void applyQueryWrapper(){

    }
}