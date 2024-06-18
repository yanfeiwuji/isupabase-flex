package io.github.yanfeiwuji.isupabase.stroage.ex;

import io.github.yanfeiwuji.isupabase.auth.ex.AuthExRes;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:46
 */

@AllArgsConstructor
@Getter
public class StorageEx extends RuntimeException {
    private final StorageExRes storageExRes;
}
