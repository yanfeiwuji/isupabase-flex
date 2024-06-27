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
    private String authPrivateKey = """
            -----BEGIN PRIVATE KEY-----
            MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCl7h7NDJ0Y6bNm0O4N4Q04o2mA7p7c3UgIX3/NZyrD1FN/9k7+3jhyi9k6M6YST7+FBSJ8QYHzjgIMn6y2J0j34PQBdDPlpOSGAMMHgE7b0pRiGWYirhLSB9j0k4b6aiELS5E4e2JmYkDGZK5rCQYmK9QQKyckg2a8aYI7T/XzDNsDRpt0n+WxzF8AtQ5HRxnTaYxTrSXOl3o3QgDMmw6sjsBZLHmrwlwg5lRYwMeiy0bPd7dUQTS07Q/gKS24/SZxGPRzFSScubMiRe40KEVSGMLtOaBnKxwJ9PaI1Kh5wgFMCRASH8SlSCL2P4sa0iy2zL6pgQbE7rlWuly4ybq5AgMBAAECggEAE8DnVc3cneykeOcCkBBsvINnufuuWejV/HkyA8f2ZV3Pmvo9qUXXPnu7npwrdx6bVECqPFSoHdCYGdygeQbwPuI+6jVlQD+DHRjWHvx9ZtoI5CzV5ecHQo3AnE070m/0Ax456Zl2u22S/Itb4E4wtzachlg8zrH99wwwftMkGo0J2F1GlmMZx5bxmw6ls2XxY6oaJ/MhheWJVT2TgQdE2RzYLHCkz5leJ9dHEfuVES+YHNVcdhxZ4re+kSqYChDNWY5Xsk2aBVW6AIb72bcQusj9ntZFP1cV3pQSDgHjYM64uoRBl24JMt26irMa+FEAnqAnBJaDJ53j+czXN8kUoQKBgQC3wswRPBCaa9/GkAeL8CpFd5Dq2kB8Hobp9APG+dSeIqnmc5ls8PtnsR3ISDy8Tj9j9U17jNKqmXuhDPM5H/qgLGdu5Zqn1YB+m5R4He7A3goKNHRoOOnd5ryYwAdKJy0AiOs2p8H24eEQIeWaVc89A4IWlTH/LHyxA6DF43SVpQKBgQDnKOH/gKAWnrCkL9Uv2WNqUqzuUYxgatyuGJ+FqkJ5MELp/4jnDaeYMesZ5xXkdJ1uSiMahl8eL5oT59gjkB9741E1u4EKCVC4bCYQJkXGR4ICjfyrTF2NfP79cgyE4wSDgrT6b0zq77vx8A5q+xYy+jS/8TcV1OqpxTGTAa9MhQKBgBNFeZNBIlifgYtYZYI2gKhiXq9Sjjq2FN4KOi8u2i5vYjQJ4Yg8SZFZUM9FTCFPb3RzMFoJrbo4eG+uZP+Tq/wKTBqkN7DsQhqFBHAIa5aMcq8ZSy/28AfWf+bhFKKhMyYANfK4ay4SCEFh3Ktv3lj+ujDWGrUxHC6QO9clAIAFAoGAafk/KqBwRA56UrsvFCGoRw0iBJvULDuxcgsNzvyQPvjWJGqm+64x4D86VgSv761aoz5Pu5BQuQj/rRKD70HafTRofaa5cL12iSXjiVHSeSU9QCiR5oI6/WuJhu2rOZzN6/CXQZyW+bnwQulX4sm7kqFMX5aZN2QGNNCXepqyH/UCgYAdpYwPDDX8FP2OrWxH6zuHMWX8NUCvbBMljKAF4CUNpEdX84s0gIWeauMsup9kOgJllk27xh8hZ0XfP1Igav2BsdCc2UW762Vr7SP6X4Xqtdqkt1Sq2XOxJP4Vndv5UA1LnYKNj3cu8felTIo9M5/f11ccVJ6agRlKI1pQI+oHGQ==
            -----END PRIVATE KEY-----
            """;
    private String authPublicKey = """
            -----BEGIN PUBLIC KEY-----
            MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEApe4ezQydGOmzZtDuDeENOKNpgO6e3N1ICF9/zWcqw9RTf/ZO/t44covZOjOmEk+/hQUifEGB844CDJ+stidI9+D0AXQz5aTkhgDDB4BO29KUYhlmIq4S0gfY9JOG+mohC0uROHtiZmJAxmSuawkGJivUECsnJINmvGmCO0/18wzbA0abdJ/lscxfALUOR0cZ02mMU60lzpd6N0IAzJsOrI7AWSx5q8JcIOZUWMDHostGz3e3VEE0tO0P4CktuP0mcRj0cxUknLmzIkXuNChFUhjC7TmgZyscCfT2iNSoecIBTAkQEh/EpUgi9j+LGtIstsy+qYEGxO65VrpcuMm6uQIDAQAB
            -----END PUBLIC KEY-----
            """;

    private Long jwtExp = 3600L;
    private Long passwordMinLength = 6L;
    private String passwordRequiredCharacters = "";
    private String siteUrl = "http://localhost:8080";

    private Long oneTimeExpiredMinutes = 5L;
    private List<String> redirectUrls = List.of();
    private Map<String, AuthConfig> authProviders = Map.of();


    private Long storageUpdateSignedJwtExp = 7200L;

    @PostConstruct
    public void init() {
        authProviders.forEach((k, v) -> v.setRedirectUri(AuthStrPool.AUTH_CALL_BACK_URL_TEMP.formatted(this.siteUrl)));
    }
}
