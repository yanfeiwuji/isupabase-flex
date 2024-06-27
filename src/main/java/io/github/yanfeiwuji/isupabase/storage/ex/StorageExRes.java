package io.github.yanfeiwuji.isupabase.storage.ex;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.github.yanfeiwuji.isupabase.constants.StorageStrPool;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 17:26
 */
public record StorageExRes(String error, String message, @JsonAnyGetter Map<String, ?> ext) {
    public static final Map<String, String> STATUS_CODE_409 = Map.of(StorageStrPool.EX_STATUS_CODE, "409");
    public static final Map<String, String> STATUS_CODE_404 = Map.of(StorageStrPool.EX_STATUS_CODE, "404");
    public static final Map<String, String> STATUS_CODE_400 = Map.of(StorageStrPool.EX_STATUS_CODE, "400");
    public static final Map<String, String> STATUS_CODE_415 = Map.of(StorageStrPool.EX_STATUS_CODE, "415");
    public static final Map<String, String> STATUS_CODE_413 = Map.of(StorageStrPool.EX_STATUS_CODE, "413");
    public static final StorageExRes RESOURCE_ALREADY_EXIST = new StorageExRes("Duplicate", "The resource already exists", STATUS_CODE_409);
    public static final StorageExRes TRIED_DELETE_NOT_EMPTY_BUCKET = new StorageExRes("InvalidRequest", "The bucket you tried to delete is not empty", STATUS_CODE_409);
    public static final StorageExRes BUCKET_NOT_FOUND = new StorageExRes("Bucket not found", "Bucket not found", STATUS_CODE_400);
    public static final StorageExRes SORT_ORDER_NOT_ALLOW = new StorageExRes("Error", "body/sortBy/order must be equal to one of the allowed values", STATUS_CODE_400);
    public static final StorageExRes SORT_COLUMN_NOT_ALLOW = new StorageExRes("Error", "body/sortBy/column must be equal to one of the allowed values", STATUS_CODE_400);

    public static final StorageExRes OBJECT_NOT_FOUND = new StorageExRes("not_found", "Object not found", STATUS_CODE_404);

    public static final StorageExRes DELETE_PREFIXES_IS_EMPTY = new StorageExRes("Error", "body/prefixes must NOT have fewer than 1 items", STATUS_CODE_400);
    public static final StorageExRes CREATE_SIGNED_EXP_IN_MUST_GE_ONE = new StorageExRes("Error", "body/expiresIn must be >= 1", STATUS_CODE_400);
    public static final StorageExRes CREATE_SIGNED_PATHS_MUST_GE_ONE = new StorageExRes("Error", "body/paths must NOT have fewer than 1 items", STATUS_CODE_400);

    public static final StorageExRes INVALID_JWT = new StorageExRes("InvalidJWT", "jwt malformed", STATUS_CODE_400);
    public static final StorageExRes INVALID_SIGNATURE = new StorageExRes("InvalidSignature", "Invalid signature", STATUS_CODE_400);

    public static final StorageExRes UPLOAD_NULL_ERROR = new StorageExRes("invalid_mime_type", "mime type text/plain;charset=UTF-8 is not supported", STATUS_CODE_415);

    public static final StorageExRes PAYLOAD_TOO_LAGER = new StorageExRes("Payload too large", "The object exceeded the maximum allowed size", STATUS_CODE_413);

    public StorageExRes(String error, String message) {
        this(error, message, Map.of());
    }

    public StorageEx toStorageEx() {
        return new StorageEx(this);
    }
}
