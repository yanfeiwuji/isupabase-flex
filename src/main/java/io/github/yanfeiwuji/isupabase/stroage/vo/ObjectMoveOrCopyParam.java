package io.github.yanfeiwuji.isupabase.stroage.vo;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author yanfeiwuji
 * @date 2024/6/23 11:08
 */
@Data
@JsonNaming
public class ObjectMoveOrCopyParam {

    @NotNull
    private String bucketId;
    @NotNull
    private String destinationKey;
    @NotNull
    private String sourceKey;
}
