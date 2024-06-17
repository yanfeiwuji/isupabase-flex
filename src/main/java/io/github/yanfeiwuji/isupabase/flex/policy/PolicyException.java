package io.github.yanfeiwuji.isupabase.flex.policy;

/**
 * @author yanfeiwuji
 * @date 2024/6/17 15:13
 */
class PolicyException extends RuntimeException {
    public PolicyException() {
        super("Only support ISelectPolicy or IInsertPolicy or IUpdatePolicy or IDeletePolicy");
    }
}
