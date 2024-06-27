package io.github.yanfeiwuji.isupabase.storage.vo;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/23 11:28
 */
@Data
@JsonNaming
public class ObjectDeleteParam {
    private List<String> prefixes;
}
