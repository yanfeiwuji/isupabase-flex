package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 14:07
 */
@EqualsAndHashCode(callSuper = true)
@Table("yfwj_session")
@Data
public class Session extends AuthBase {

    private Long userId;

    private Long factorId;

    private EAalLevel aal;

    private OffsetDateTime notAfter;
    private OffsetDateTime refreshedAt;
    private String userAgent;
    private String ip;
    private String tag;
}
