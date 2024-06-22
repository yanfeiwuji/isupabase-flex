package io.github.yanfeiwuji.isupabase.stroage.vo;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.yanfeiwuji.isupabase.stroage.entity.ObjectMetadata;
import io.github.yanfeiwuji.isupabase.stroage.entity.StorageObject;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/21 12:02
 */
@Data
public class StorageVo {
    private String id;
    private OffsetDateTime createdAt;
    private OffsetDateTime lastAccessedAt;

    private OffsetDateTime updatedAt;
    private ObjectMetadata metadata;
    private String name;
    @JsonIgnore
    private boolean dir = false;

    public static StorageVo of(StorageObject object, String prefix) {
        Objects.requireNonNull(object);

        StorageVo vo = new StorageVo();
        final String nextName = CharSequenceUtil.removePrefix(object.getName(), prefix + StrPool.SLASH);
        if (CharSequenceUtil.containsIgnoreCase(nextName, StrPool.SLASH)) {
            // dir

            final List<String> split = CharSequenceUtil.split(nextName, StrPool.C_SLASH, 2);
            vo.setId(StrUtil.toString(object.getId()));
            vo.setName(split.getFirst());
            vo.setDir(true);
        } else {
            vo.setName(nextName);
            vo.setCreatedAt(object.getCreatedAt());
            vo.setLastAccessedAt(object.getLastAccessedAt());
            vo.setUpdatedAt(object.getUpdatedAt());
            vo.setMetadata(object.getMetadata());
            vo.setId(StrUtil.toString(object.getId()));
        }
        return vo;

    }

}
