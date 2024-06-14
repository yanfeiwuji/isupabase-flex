package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 15:00
 */
@EqualsAndHashCode(callSuper = true)
@Table("yfwj_identity")
@Data
public class Identity extends AuthBase {


    private Long userId;

    @Column(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> identityData;
    private String provider;
    private String providerId;
    private OffsetDateTime lastSignInAt;


    private String email;

}
