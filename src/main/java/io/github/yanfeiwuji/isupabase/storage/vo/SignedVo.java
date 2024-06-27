package io.github.yanfeiwuji.isupabase.storage.vo;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author yanfeiwuji
 * @date 2024/6/23 15:42
 */
@JsonNaming
public record SignedVo(String signedURL) {
}
