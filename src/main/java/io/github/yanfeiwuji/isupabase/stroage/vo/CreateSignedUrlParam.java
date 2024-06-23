package io.github.yanfeiwuji.isupabase.stroage.vo;


import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * @author yanfeiwuji
 * @date 2024/6/23 15:35
 */
@JsonNaming
public record CreateSignedUrlParam(Long expiresIn) {

}
