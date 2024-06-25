package io.github.yanfeiwuji.isupabase.stroage.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * @author yanfeiwuji
 * @date 2024/6/21 12:02
 */
@Data
@JsonNaming
public class ObjectMetadata {
    private String cacheControl;

    private Long contentLength;
    @JsonProperty("eTag")
    private String eTag;
    private Long httpStatusCode;
    private OffsetDateTime lastModified;
    private String mimetype;
    private Long size;
}
