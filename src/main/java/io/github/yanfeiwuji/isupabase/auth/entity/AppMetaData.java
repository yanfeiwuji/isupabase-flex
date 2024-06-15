package io.github.yanfeiwuji.isupabase.auth.entity;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/15 18:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppMetaData {
    private String provider;
    private List<String> providers;

    public AppMetaData(String provider) {
        this(provider, List.of(provider));
    }

    public static final AppMetaData EMAIL_APP_META_DATA = new AppMetaData(AuthStrPool.IDENTITY_PROVIDER_EMAIL);

    public static AppMetaData addProvider(AppMetaData appMetaData, String provider) {

        final List<String> providers = Optional.ofNullable(appMetaData)
                .map(AppMetaData::getProviders).orElse(Collections.singletonList(provider));
        final AppMetaData need = Optional.ofNullable(appMetaData).orElse(new AppMetaData(provider));
        need.setProviders(providers);
        return need;


    }
}
