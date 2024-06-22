package io.github.yanfeiwuji.isupabase.stroage.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 17:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("storage_object")
public class StorageObject extends StorageBase {
    @Id(keyType = KeyType.Generator, value = KeyGenerators.flexId)
    private Long id;

    private String bucketId;
    private Long owner;
    private String name;
    @Column(onInsertValue = "now()", onUpdateValue = "now()")
    private OffsetDateTime lastAccessedAt;
    @Column(typeHandler = JacksonTypeHandler.class)
    private ObjectMetadata metadata;
    @Column(typeHandler = JacksonTypeHandler.class)
    private List<String> pathTokens;
    private String version;
    private String ownerId;

}
