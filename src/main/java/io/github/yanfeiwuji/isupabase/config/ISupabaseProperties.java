package io.github.yanfeiwuji.isupabase.config;

import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import me.zhyd.oauth.config.AuthConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 16:32
 */
@ConfigurationProperties(prefix = "isupabase")
@Configuration
@Data
public class ISupabaseProperties {

    private Long maxRows = 986L;
    private String authPrivateKey;
    private String authPublicKey;

    private Long jwtExp = 3600L;
    private Long passwordMinLength = 6L;
    private String passwordRequiredCharacters = "";
    private String siteUrl;

    private Long oneTimeExpiredMinutes = 5L;
    private List<String> redirectUrls = List.of();
    private Map<String, AuthConfig> authProviders = Map.of();


    @PostConstruct
    public void init() {
        // final HttpConfig httpConfig = HttpConfig.builder()
        // .timeout(15000)
        // .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 1082)))
        // .build();
        authProviders.forEach((k, v) -> {
            v.setRedirectUri(AuthStrPool.AUTH_CALL_BACK_URL_TEMP.formatted(this.siteUrl));
            // v.setHttpConfig(httpConfig); //v.setHttpConfig();
        });

    }
}
