package io.github.yanfeiwuji.isupabase.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import com.mybatisflex.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/26 10:04
 */
@Getter
@AllArgsConstructor
public enum TestType {
    a(0),b(1);;
    @EnumValue
    @JsonValue
    private Integer code;
}
