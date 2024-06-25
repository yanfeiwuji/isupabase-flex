package io.github.yanfeiwuji.isupabase.stroage.provider;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import io.github.yanfeiwuji.isupabase.stroage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.stroage.entity.ObjectMetadata;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageExFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/25 08:56
 */

public class DefaultS3Provider implements S3Provider<Resource> {
    private static final String S3_DIR = "s3";
    private final Path s3DirPath;


    public DefaultS3Provider() {
        Path currentDir = Paths.get(CharSequenceUtil.EMPTY).toAbsolutePath();
        this.s3DirPath = currentDir.resolve(S3_DIR);
        if (!Files.exists(s3DirPath)) {

            try {
                Files.createDirectory(s3DirPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    public ObjectMetadata putObject(Bucket bucket, String key, String cacheControl, String mineType, byte[] fileByte) {
        final String id = bucket.getId();
        final Path bucketDir = s3DirPath.resolve(id);
        if (!Files.exists(bucketDir)) {
            try {
                Files.createDirectory(bucketDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            final String md5 = md5(fileByte);
            final Path filePath = bucketDir.resolve(md5);
            if (!Files.exists(filePath)) {
                Files.write(filePath, fileByte);
            }
            final ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setETag(md5);
            objectMetadata.setSize((long) fileByte.length);
            objectMetadata.setCacheControl("max-age=%s".formatted(cacheControl));
            objectMetadata.setMimetype(mineType);
            objectMetadata.setLastModified(OffsetDateTime.now());
            objectMetadata.setContentLength((long) fileByte.length);
            objectMetadata.setHttpStatusCode(200L);

            return objectMetadata;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ResponseEntity<Resource> downloadObject(Bucket bucket, String key, String download, ObjectMetadata objectMetadata) {
        final Path bucketDir = s3DirPath.resolve(bucket.getId());
        final String eTag = objectMetadata.getETag();
        if (Objects.isNull(eTag)) {
            throw StorageExFactory.bucketNotFound();
        }
        final Path filePath = bucketDir.resolve(eTag);
        UrlResource urlResource;
        try {
            urlResource = new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        final String cacheControl = bucket.isPublicBucket() ? "public, " + objectMetadata.getCacheControl() : objectMetadata.getCacheControl();
        final String mimetype = objectMetadata.getMimetype();
        Instant lastModified = Instant.from(objectMetadata.getLastModified());
        final ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok()
                .contentLength(objectMetadata.getContentLength())
                .header("Cache-Control", cacheControl)
                .contentType(MediaType.parseMediaType(mimetype))
                .lastModified(lastModified)
                .eTag(eTag);
        if (Objects.nonNull(download)) {
            bodyBuilder.header("Content-Disposition", "attachment; filename=\"" + download + "\"");
        }

        return bodyBuilder.body(urlResource);
    }

    private String md5(byte[] bytes) {
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        messageDigest.update(bytes);
        byte[] hashBytes = messageDigest.digest();
        StringBuilder sb = new StringBuilder();

        for (byte b : hashBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
