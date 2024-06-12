package io.github.yanfeiwuji.isupabase.auth.entity;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import com.fasterxml.jackson.annotation.JsonValue;
import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 14:16
 */
@AllArgsConstructor
@Getter
public enum EAalLevel {

    ALL_1("all1"), ALL_2("aal2"), ALL_3("aal3");
    @EnumValue
    @JsonValue
    private final String code;
}
