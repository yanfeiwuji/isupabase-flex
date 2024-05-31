package io.github.yanfeiwuji.isupabase.config;

import java.util.Map;

public record PgrstJsonModifier(String relEnd, Map<String, String> kv,
                                PgrstJsonModifier next
) {

}
