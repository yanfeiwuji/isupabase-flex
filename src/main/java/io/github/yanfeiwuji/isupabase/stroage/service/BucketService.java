package io.github.yanfeiwuji.isupabase.stroage.service;

import io.github.yanfeiwuji.isupabase.stroage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageExFactory;
import io.github.yanfeiwuji.isupabase.stroage.mapper.BucketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;


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
}
