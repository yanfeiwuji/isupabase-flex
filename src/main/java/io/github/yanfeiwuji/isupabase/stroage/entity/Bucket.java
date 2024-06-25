package io.github.yanfeiwuji.isupabase.stroage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.handler.JacksonTypeHandler;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 16:51
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Table("storage_bucket")
public class Bucket extends StorageBase {
    @Id
    private String id;
    private String name;
    private Long owner;
    @Column(value = "public")
    @JsonProperty(value = "public")

    private boolean publicBucket;
    private boolean avifAutodetection;

    @Column(typeHandler = JacksonTypeHandler.class)
//    @Pattern(regexp = "*/*")
    private List<String> allowedMimeTypes;

    private Long fileSizeLimit;

    private Long ownerId;


}
