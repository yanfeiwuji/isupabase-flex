package io.github.yanfeiwuji.isupabase.stroage.ex;

import io.github.yanfeiwuji.isupabase.auth.ex.AuthEx;
import io.github.yanfeiwuji.isupabase.request.ex.PgrstExInfo;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.function.Supplier;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:48
 */
@UtilityClass
public class StorageExFactory {
    public static StorageEx RESOURCE_ALREADY_EXIST = StorageExRes.RESOURCE_ALREADY_EXIST.toStorageEx();

    public static StorageEx TRIED_DELETE_NOT_EMPTY_BUCKET = StorageExRes.TRIED_DELETE_NOT_EMPTY_BUCKET.toStorageEx();

    public static StorageEx BUCKET_NOT_FOUND = StorageExRes.BUCKET_NOT_FOUND.toStorageEx();
    public static StorageEx SORT_ORDER_NOT_ALLOW = StorageExRes.SORT_ORDER_NOT_ALLOW.toStorageEx();
    public static StorageEx SORT_COLUMN_NOT_ALLOW = StorageExRes.SORT_COLUMN_NOT_ALLOW.toStorageEx();
    public static StorageEx OBJECT_NOT_FOUND = StorageExRes.OBJECT_NOT_FOUND.toStorageEx();

    public Supplier<StorageEx> invalidKey(String key) {
        return () -> new StorageExRes("InvalidKey", "Invalid key: %s".formatted(key), StorageExRes.STATUS_CODE_400).toStorageEx();
    }

    public StorageEx bucketNotFound() {
        return BUCKET_NOT_FOUND;
    }

    public StorageEx resourceAlreadyExist() {
        return RESOURCE_ALREADY_EXIST;
    }


}
