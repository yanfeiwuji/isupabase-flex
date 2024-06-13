package io.github.yanfeiwuji.isupabase.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author yanfeiwuji
 * @date 2024/6/10 16:32
 */
@ConfigurationProperties(prefix = "isupabase")
@Configuration
@Data
public class ISupabaseProperties {

    private String authPrivateKey;
    private String authPublicKey;

    private Long jwtExp = 3600L;
    private Long passwordMinLength = 6L;
    private String passwordRequiredCharacters = "";
    private String siteUrl;

    private Long emailLinkExpiredMinutes = 5L;

}
