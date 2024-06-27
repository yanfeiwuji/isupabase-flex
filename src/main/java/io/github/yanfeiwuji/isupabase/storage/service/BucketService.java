package io.github.yanfeiwuji.isupabase.storage.service;

import io.github.yanfeiwuji.isupabase.storage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.storage.ex.StorageExFactory;
import io.github.yanfeiwuji.isupabase.storage.mapper.BucketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.github.yanfeiwuji.isupabase.storage.entity.table.BucketTableDef.BUCKET;


/**
 * @author yanfeiwuji
 * @date 2024/6/20 16:17
 */
@Service
@RequiredArgsConstructor
public class BucketService {

    private final BucketMapper bucketMapper;

    public Bucket nNBucketById(String id) {
        return Optional.ofNullable(id).map(bucketMapper::selectOneById).orElseThrow(StorageExFactory::bucketNotFound);
    }

    public Bucket nNPublicBucketById(String id) {

        return Optional.ofNullable(id).map(BUCKET.ID::eq)
                .map(it -> it.and(BUCKET.PUBLIC_BUCKET.eq(true)))
                .map(bucketMapper::selectOneByCondition)
                .orElseThrow(StorageExFactory::bucketNotFound);
    }
}
