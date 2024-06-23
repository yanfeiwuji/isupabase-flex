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
    public static final StorageEx RESOURCE_ALREADY_EXIST = StorageExRes.RESOURCE_ALREADY_EXIST.toStorageEx();

    public static final StorageEx TRIED_DELETE_NOT_EMPTY_BUCKET = StorageExRes.TRIED_DELETE_NOT_EMPTY_BUCKET.toStorageEx();

    public static final StorageEx BUCKET_NOT_FOUND = StorageExRes.BUCKET_NOT_FOUND.toStorageEx();
    public static final StorageEx SORT_ORDER_NOT_ALLOW = StorageExRes.SORT_ORDER_NOT_ALLOW.toStorageEx();
    public static final StorageEx SORT_COLUMN_NOT_ALLOW = StorageExRes.SORT_COLUMN_NOT_ALLOW.toStorageEx();
    public static final StorageEx OBJECT_NOT_FOUND = StorageExRes.OBJECT_NOT_FOUND.toStorageEx();
    public static final StorageEx DELETE_PREFIXES_IS_EMPTY = StorageExRes.DELETE_PREFIXES_IS_EMPTY.toStorageEx();
    public static final StorageEx CREATE_SIGNED_EXP_IN_MUST_GE_ONE = StorageExRes.CREATE_SIGNED_EXP_IN_MUST_GE_ONE.toStorageEx();

    public static final StorageEx CREATE_SIGNED_PATHS_MUST_GE_ONE = StorageExRes.CREATE_SIGNED_PATHS_MUST_GE_ONE.toStorageEx();

    public static final StorageEx INVALID_JWT = StorageExRes.INVALID_JWT.toStorageEx();
    public static final StorageEx INVALID_SIGNATURE = StorageExRes.INVALID_SIGNATURE.toStorageEx();

    public Supplier<StorageEx> invalidKey(String key) {
        return () -> new StorageExRes("InvalidKey", "Invalid key: %s".formatted(key), StorageExRes.STATUS_CODE_400).toStorageEx();
    }

    public StorageEx invalidSignature() {
        return INVALID_SIGNATURE;
    }

    public StorageEx createSignedExpInMustGeOne() {
        return CREATE_SIGNED_EXP_IN_MUST_GE_ONE;
    }

    public StorageEx bucketNotFound() {
        return BUCKET_NOT_FOUND;
    }

    public StorageEx resourceAlreadyExist() {
        return RESOURCE_ALREADY_EXIST;
    }


}
