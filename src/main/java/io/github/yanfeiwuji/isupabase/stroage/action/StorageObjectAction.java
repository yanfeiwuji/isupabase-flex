package io.github.yanfeiwuji.isupabase.stroage.action;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.row.Row;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtils;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import io.github.yanfeiwuji.isupabase.constants.StorageStrPool;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstDb;
import io.github.yanfeiwuji.isupabase.stroage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.stroage.entity.StorageObject;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageExFactory;
import io.github.yanfeiwuji.isupabase.stroage.mapper.StorageObjectMapper;
import io.github.yanfeiwuji.isupabase.stroage.service.BucketService;
import io.github.yanfeiwuji.isupabase.stroage.vo.ObjectListParam;
import io.github.yanfeiwuji.isupabase.stroage.vo.ObjectListParamOrder;
import io.github.yanfeiwuji.isupabase.stroage.vo.StorageShortInfo;
import io.github.yanfeiwuji.isupabase.stroage.vo.StorageVo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.yanfeiwuji.isupabase.stroage.entity.table.StorageObjectTableDef.STORAGE_OBJECT;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 18:51
 */
@RequestMapping(StorageStrPool.STORAGE_PATH + "/object")
@RestController
@RequiredArgsConstructor

public class StorageObjectAction {
    private static final Pattern KEY_PATTERN = Pattern.compile("^([a-zA-Z0-9_]+/)*[a-zA-Z0-9_]+$");
    private final BucketService bucketService;
    private final StorageObjectMapper storageObjectMapper;
    private final PgrstDb pgrstDb;

    @PostMapping("/{bucketId}/**")
    public StorageShortInfo upload(@PathVariable String bucketId, HttpServletRequest request) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);

        final String key = pickKey(bucketId, request);


        final long count = storageObjectMapper.selectCountByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));

        if (count > 0) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }
        final StorageObject storageObject = new StorageObject();
        storageObject.setBucketId(bucket.getId());
        storageObject.setName(key);
        storageObject.setOwner(AuthUtils.uid().orElse(-1L));
        storageObject.setOwnerId(AuthUtils.uid().orElse(-1L).toString());
        storageObject.setLastAccessedAt(OffsetDateTime.now());
        storageObject.setPathTokens(CharSequenceUtil.split(key, StrPool.SLASH));

        pgrstDb.insertSelective(storageObjectMapper, storageObject);

        //  todo consider has update success
        return new StorageShortInfo(String.valueOf(storageObject.getId()), key);
    }


    @PostMapping("/list/{bucketId}")
    public List<StorageVo> list(@PathVariable String bucketId,
                                @RequestBody ObjectListParam param
    ) {
        final QueryWrapper queryWrapper = param.toQueryWrapper(bucketId);
        final List<StorageObject> storageObjects = pgrstDb.selectListByQuery(storageObjectMapper, queryWrapper);

        final List<Long> ids = storageObjects.stream().map(StorageObject::getId).toList();
        final Map<String, StorageVo> info = storageObjectMapper.selectListByIds(ids)
                .stream().map(it -> StorageVo.of(it, param.prefix()))
                .collect(Collectors.toMap(StorageVo::getId, it -> it));

        return ids.stream().map(String::valueOf).map(info::get)
                .peek(it -> {
                    if (it.isDir()) {
                        it.setId(null);
                    }
                })
                .toList();
    }

    @PutMapping("/{bucketId}/**")
    public StorageShortInfo update(@PathVariable String bucketId, HttpServletRequest request) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);

        final String key = pickKey(bucketId, request);
        final StorageObject dbStorageObject = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));
        if (Objects.isNull(dbStorageObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }

        dbStorageObject.setOwner(AuthUtils.uid().orElse(-1L));
        dbStorageObject.setOwnerId(AuthUtils.uid().orElse(-1L).toString());
        dbStorageObject.setLastAccessedAt(OffsetDateTime.now());

       pgrstDb.update(storageObjectMapper, dbStorageObject);

        storageObjectMapper.update(dbStorageObject);
        //  todo consider has update success
        return new StorageShortInfo(String.valueOf(dbStorageObject.getId()), key);
    }


    private String pickKey(String bucketId, HttpServletRequest request) {
        String key = Optional.ofNullable(request).map(HttpServletRequest::getServletPath)
                .map(it -> it.split(bucketId, 2))
                .filter(it -> it.length >= 2)
                .map(it -> it[1])
                .map(it -> CharSequenceUtil.removePrefix(it, StrPool.SLASH))
                .orElse(CharSequenceUtil.EMPTY);

        if (key.equals(CharSequenceUtil.EMPTY) || !KEY_PATTERN.matcher(key).matches()) {
            throw StorageExFactory.invalidKey(key).get();
        }
        return key;
    }

}
