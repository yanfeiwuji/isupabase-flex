package io.github.yanfeiwuji.isupabase.stroage.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 16:48
 */
@Data
public class StorageBase {


    @Column(onInsertValue = "now()")
    private OffsetDateTime createdAt;
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private OffsetDateTime updatedAt;
}
