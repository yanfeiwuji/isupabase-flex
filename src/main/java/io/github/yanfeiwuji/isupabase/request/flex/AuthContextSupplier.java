package io.github.yanfeiwuji.isupabase.request.flex;

import java.util.function.Supplier;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 14:00
 */
@FunctionalInterface
public interface AuthContextSupplier<T extends AuthContext> extends Supplier<T> {

}
