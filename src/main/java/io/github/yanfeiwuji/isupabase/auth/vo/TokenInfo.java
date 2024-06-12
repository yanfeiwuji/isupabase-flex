package io.github.yanfeiwuji.isupabase.auth.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 12:03
 */
@Data
@Builder
public class TokenInfo<T> {
    private String accessToken;

    private Long expiresAt;
    private Long expiresIn;
    private String refreshToken;
    private String tokenType;
    private T user;
}
