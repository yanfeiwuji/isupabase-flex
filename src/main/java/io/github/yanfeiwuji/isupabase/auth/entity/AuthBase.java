package io.github.yanfeiwuji.isupabase.auth.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 18:05
 */

@Data
public abstract class AuthBase {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    @Column(onInsertValue = "now()")
    private OffsetDateTime createdAt;
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private OffsetDateTime updatedAt;
}
