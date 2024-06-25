package io.github.yanfeiwuji.isupabase.stroage.action;

import cn.hutool.core.text.CharSequenceUtil;
import com.mybatisflex.core.query.QueryCondition;
import io.github.yanfeiwuji.isupabase.constants.StorageStrPool;
import io.github.yanfeiwuji.isupabase.stroage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.stroage.entity.StorageObject;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageExFactory;
import io.github.yanfeiwuji.isupabase.stroage.mapper.BucketMapper;
import io.github.yanfeiwuji.isupabase.stroage.mapper.StorageObjectMapper;
import io.github.yanfeiwuji.isupabase.stroage.vo.BucketName;
import io.github.yanfeiwuji.isupabase.stroage.vo.StorageMessage;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static io.github.yanfeiwuji.isupabase.stroage.entity.table.BucketTableDef.BUCKET;
import static io.github.yanfeiwuji.isupabase.stroage.entity.table.StorageObjectTableDef.STORAGE_OBJECT;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 16:39
 */
@RequestMapping(StorageStrPool.STORAGE_PATH + "/bucket")
@RestController
@RequiredArgsConstructor
public class StorageBucketAction {
    private final BucketMapper bucketMapper;
    private final StorageObjectMapper storageObjectsMapper;

    @PostMapping
    public BucketName post(@RequestBody Bucket bucket) {
        final Bucket dbBucket = bucketMapper.selectOneById(bucket.getId());

        if (Objects.nonNull(dbBucket)) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }

        final List<String> endAllow = Optional.ofNullable(bucket.getAllowedMimeTypes())
                .orElse(List.of())
                .stream().filter(it -> CharSequenceUtil.isNotEmpty(it) && CharSequenceUtil.isNotBlank(it))
                .map(it -> {
                    try {
                        MimeType mimeType = MimeTypeUtils.parseMimeType(it);
                        return mimeType.toString();
                    } catch (InvalidMimeTypeException e) {
                        throw StorageExFactory.invalidMimeType(it).get();
                    }
                }).toList();

        bucket.setAllowedMimeTypes(endAllow);

        bucketMapper.insert(bucket);
        return new BucketName(bucket.getName());
    }

    @DeleteMapping("{id}")
    public StorageMessage delete(@PathVariable("id") @NotNull String id) {

        final StorageObject storageObjects = storageObjectsMapper.selectOneByCondition(STORAGE_OBJECT.BUCKET_ID.eq(id));
        if (Objects.nonNull(storageObjects)) {
            throw StorageExFactory.TRIED_DELETE_NOT_EMPTY_BUCKET;
        }
        bucketMapper.deleteByCondition(BUCKET.ID.eq(id));

        return StorageMessage.SUCCESS_DELETED;
    }

    @PostMapping("{id}/empty")
    public StorageMessage emptyBucket(@PathVariable("id") @NotNull String id) {
        storageObjectsMapper.deleteByCondition(STORAGE_OBJECT.BUCKET_ID.eq(id));
        return StorageMessage.SUCCESS_EMPTIED;
    }

    @GetMapping("{id}")
    public Bucket get(@PathVariable("id") @NotNull String id) {
        final Bucket bucket = bucketMapper.selectOneByCondition(BUCKET.ID.eq(id));
        if (Objects.nonNull(bucket)) {
            return bucket;
        }
        throw StorageExFactory.BUCKET_NOT_FOUND;
    }

    @GetMapping
    public List<Bucket> list() {
        return bucketMapper.selectListByCondition(QueryCondition.createEmpty());
    }


    @PutMapping("{id}")
    public StorageMessage put(@PathVariable("id") @NotNull String id, @RequestBody Bucket bucket) {

        final int i = bucketMapper.updateByCondition(bucket, BUCKET.ID.eq(id));
        if (i > 0) {
            return StorageMessage.SUCCESS_UPDATED;
        }
        throw StorageExFactory.BUCKET_NOT_FOUND;
    }

}
