package io.github.yanfeiwuji.isupabase.flex;

/**
 * @author yanfeiwuji
 * @date 2024/6/16 17:52
 */
public class InvokeContextHolder {
    private static final ThreadLocal<InvokeContext> invokeContext = new ThreadLocal<>();

    public static InvokeContext get() {
        return invokeContext.get();
    }

    public static void set(InvokeContext invokeContext) {
        InvokeContextHolder.invokeContext.set(invokeContext);
    }

    public static void remove() {
        invokeContext.remove();
    }


 
}
