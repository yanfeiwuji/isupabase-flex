package io.github.yanfeiwuji.isupabase.storage.vo;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/23 16:03
 */
@JsonNaming
public record CreateSignedUrlsParam(Long expiresIn, List<String> paths) {
}
