# config

All configurations are contained within the ISupabaseProperties class.
::: danger
To ensure that the project's API key remains consistent every time, the public and private keys are hard-coded here. In
a production environment, you must configure your own keys and ensure they are never leaked.
:::

```java

@ConfigurationProperties(prefix = "isupabase")
@Configuration
@Data
public class ISupabaseProperties {

    // Maximum number of rows returned
    private Long maxRows = 986L;

    // JWT encode private key using RSA
    private String authPrivateKey = """
            -----BEGIN PRIVATE KEY-----
            YOUR KEY
             -----END PRIVATE KEY-----
            """;
    // JWT encode public key using RSA
    private String authPublicKey = """
            -----BEGIN PUBLIC KEY-----
            YOUR KEY
            -----END PUBLIC KEY-----
            """;

    // JWT expiration time in seconds
    private Long jwtExp = 3600L;
    // Minimum length of passwords

    private Long passwordMinLength = 6L;

    // Required characters for passwords
    private String passwordRequiredCharacters = "";
    // Site URL used in auth callback
    private String siteUrl = "http://localhost:8080";

    // Expiry time in minutes for one-time tokens
    private Long oneTimeExpiredMinutes = 5L;
    // Allowed redirect URLs; localhost is always allowed

    private List<String> redirectUrls = List.of();
    // Configuration for authRequest's providers
    private Map<String, AuthConfig> authProviders = Map.of();

    // Storage update signed JWT expiration time in seconds
    private Long storageUpdateSignedJwtExp = 7200L;

}
```