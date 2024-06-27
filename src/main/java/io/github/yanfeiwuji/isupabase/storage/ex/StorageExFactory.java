package io.github.yanfeiwuji.isupabase.storage.ex;

import lombok.experimental.UtilityClass;

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
    public static final StorageEx UPLOAD_NULL_ERROR = StorageExRes.UPLOAD_NULL_ERROR.toStorageEx();

    public static final StorageEx PAYLOAD_TOO_LAGER = StorageExRes.PAYLOAD_TOO_LAGER.toStorageEx();

    public StorageEx uploadNullError() {
        return UPLOAD_NULL_ERROR;
    }

    public Supplier<StorageEx> invalidKey(String key) {
        return () -> new StorageExRes("InvalidKey", "Invalid key: %s".formatted(key), StorageExRes.STATUS_CODE_400).toStorageEx();
    }

    public Supplier<StorageEx> invalidMimeType(String mimeType) {
        return () -> new StorageExRes("invalid_mime_type", "mime type %s is not supported".formatted(mimeType), StorageExRes.STATUS_CODE_415).toStorageEx();

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
