package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 18:20
 */
@EqualsAndHashCode(callSuper = true)
@Table("yfwj_one_time_token")
@Data
public class OneTimeToken extends AuthBase {
    private Long userId;
    private ETokenType tokenType;
    private String tokenHash;
    private String relatesTo;
}
