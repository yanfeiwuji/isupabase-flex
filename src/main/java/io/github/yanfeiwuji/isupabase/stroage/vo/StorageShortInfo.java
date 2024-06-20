package io.github.yanfeiwuji.isupabase.stroage.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author yanfeiwuji
 * @date 2024/6/20 17:00
 */
public record StorageShortInfo(@JsonProperty("Id") String id, @JsonProperty("Key") String key) {
}
