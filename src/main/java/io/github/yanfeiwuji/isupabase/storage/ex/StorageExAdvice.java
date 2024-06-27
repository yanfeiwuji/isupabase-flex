package io.github.yanfeiwuji.isupabase.storage.ex;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:47
 */
@RestControllerAdvice
public class StorageExAdvice {
    @ExceptionHandler(value = StorageEx.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public StorageExRes storageEx(StorageEx storageEx) {
        return storageEx.getStorageExRes();
    }


}
