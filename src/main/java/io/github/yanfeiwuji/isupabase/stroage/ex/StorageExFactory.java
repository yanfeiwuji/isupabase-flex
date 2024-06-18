package io.github.yanfeiwuji.isupabase.stroage.ex;

import io.github.yanfeiwuji.isupabase.auth.ex.AuthEx;
import lombok.experimental.UtilityClass;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:48
 */
@UtilityClass
public class StorageExFactory {
    public static StorageEx RESOURCE_ALREADY_EXIST = StorageExRes.RESOURCE_ALREADY_EXIST.toStorageEx();

    public static StorageEx TRIED_DELETE_NOT_EMPTY_BUCKET = StorageExRes.TRIED_DELETE_NOT_EMPTY_BUCKET.toStorageEx();

    public static StorageEx BUCKET_NOT_FOUND = StorageExRes.BUCKET_NOT_FOUND.toStorageEx();

}
