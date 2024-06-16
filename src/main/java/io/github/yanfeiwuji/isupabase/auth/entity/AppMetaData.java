package io.github.yanfeiwuji.isupabase.auth.entity;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 18:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppMetaData implements Serializable {
    private String provider;
    private Set<String> providers;

    public AppMetaData(String provider) {
        this(provider, Set.of(provider));
    }

    public static final AppMetaData EMAIL_APP_META_DATA = new AppMetaData(AuthStrPool.IDENTITY_PROVIDER_EMAIL);

    public static AppMetaData addProvider(AppMetaData appMetaData, String provider) {
        if (Objects.isNull(appMetaData)) {
            return new AppMetaData(provider);
        } else {
            final Set<String> needProviders = appMetaData.getProviders();
            final Set<String> collect = Stream.concat(needProviders.stream(), Stream.of(provider))
                    .collect(Collectors.toSet());
            appMetaData.setProviders(collect);
            return appMetaData;
        }
    }

}
