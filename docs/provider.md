# Customize Your Provider

All providers have a local default implementation. You can provide a Spring bean to replace it.

## Auth Provider
Use JustAuth implement it. You can return an AuthRequest to use your authentication server.
You can learn more about [JustAuth](https://github.com/justauth/JustAuth) and how to use it to implement authentication.

```java
public interface AuthRequestProvider extends BiFunction<String, AuthConfig, Optional<AuthRequest>> {
}
```
The default implementation is `DefaultAuthRequestProvider`, which includes all `AuthRequest` in `AuthDefaultSource`.
## Email Message Provider
```java
public interface AuthMimeMessagePreparationProvider {
    AuthMimeMessagePreparator ofSignup(String email, MessageParam messageParam);

    AuthMimeMessagePreparator ofResetPassword(String email, MessageParam messageParam);

    AuthMimeMessagePreparator ofEmailChange(String email, MessageParam messageParam);
}
```
The default implementation is `DefaultAuthMimeMessagePreparationProvider`, which is the same as the default message in Supabase.
## Storage Provider
```java
public interface S3Provider<T> {

    ObjectMetadata putObject(Bucket bucket,
                             String key,
                             String cacheControl,
                             String mineType, byte[] fileByte);

    ResponseEntity<T> downloadObject(Bucket bucket, String key, String download, ObjectMetadata objectMetadata);
}
```
The default implementation is `DefaultS3Provider`, which saves objects to `./s3/bucketName/fileMd5value`.
