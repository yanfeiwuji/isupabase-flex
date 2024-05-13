package io.github.yanfeiwuji.isupabase.request.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

import java.util.Optional;

public class SimpleOperatorHandler   {
    @AllArgsConstructor
    @Getter
    public enum SimpleOperator {
        NEQ("neq"),
        CS("cs"),
        CD("cd"),
        OV("ov"),
        SL("sl"),
        SR("sr"),
        NXL("nxl"),
        NXR("nxr"),
        ADJ("adj");
        private String mark;

        public static Optional<SimpleOperator> valueToSimpleOperator(String value) {
            return Arrays.stream(SimpleOperator.values())
                    .filter(it -> value.startsWith(it.getMark()))
                    .findFirst();
        }
    }





}
