package io.github.yanfeiwuji.isupabase.stroage.ex;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import io.github.yanfeiwuji.isupabase.constants.StorageStrPool;

import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 17:26
 */
public record StorageExRes(String error, String message, @JsonAnyGetter Map<String, Object> ext) {
    public static final StorageExRes RESOURCE_ALREADY_EXIST = new StorageExRes("Duplicate", "The resource already exists", Map.of(StorageStrPool.EX_STATUS_CODE, "409"));
    public static final StorageExRes TRIED_DELETE_NOT_EMPTY_BUCKET = new StorageExRes("InvalidRequest", "The bucket you tried to delete is not empty", Map.of(StorageStrPool.EX_STATUS_CODE, "409"));
    public static final StorageExRes BUCKET_NOT_FOUND = new StorageExRes("Bucket not found", "Bucket not found", Map.of(StorageStrPool.EX_STATUS_CODE, "404"));

    public StorageExRes(String error, String message) {
        this(error, message, Map.of());
    }

    public StorageEx toStorageEx() {
        return new StorageEx(this);
    }
}
