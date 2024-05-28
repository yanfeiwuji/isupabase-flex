package io.github.yanfeiwuji.isupabase.request.ex;

import java.util.List;

public record ExResArgs(List<String> detailsArgs, List<String> hintArgs, List<String> messageArgs) {

    public static void msgArg() {

    }

}
