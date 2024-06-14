package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author yanfeiwuji
 * @date 2024/6/12 18:02
 */
@EqualsAndHashCode(callSuper = true)
@Table("yfwj_refresh_token")
@Data
public class RefreshToken extends AuthBase {


    private Long instanceId;
    private String token;

    private Long userId;
    private Boolean revoked;
    private String parent;

    private Long sessionId;

}
