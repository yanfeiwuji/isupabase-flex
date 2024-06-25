package io.github.yanfeiwuji.isupabase.stroage.provider;

import io.github.yanfeiwuji.isupabase.stroage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.stroage.entity.ObjectMetadata;
import org.springframework.http.ResponseEntity;

/**
 * @author yanfeiwuji
 * @date 2024/6/24 16:52
 */
public interface S3Provider<T> {

    ObjectMetadata putObject(Bucket bucket,
                             String key,
                             String cacheControl,
                             String mineType, byte[] fileByte);

    ResponseEntity<T> downloadObject(Bucket bucket, String key, String download, ObjectMetadata objectMetadata);
}
