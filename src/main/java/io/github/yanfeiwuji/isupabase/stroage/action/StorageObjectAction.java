package io.github.yanfeiwuji.isupabase.stroage.action;

import cn.hutool.core.text.CharPool;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtils;
import io.github.yanfeiwuji.isupabase.constants.StorageStrPool;
import io.github.yanfeiwuji.isupabase.request.flex.PgrstDb;
import io.github.yanfeiwuji.isupabase.stroage.entity.Bucket;
import io.github.yanfeiwuji.isupabase.stroage.entity.ObjectMetadata;
import io.github.yanfeiwuji.isupabase.stroage.entity.StorageObject;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageEx;
import io.github.yanfeiwuji.isupabase.stroage.ex.StorageExFactory;
import io.github.yanfeiwuji.isupabase.stroage.mapper.StorageObjectMapper;
import io.github.yanfeiwuji.isupabase.stroage.provider.S3Provider;
import io.github.yanfeiwuji.isupabase.stroage.service.BucketService;
import io.github.yanfeiwuji.isupabase.stroage.vo.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.github.yanfeiwuji.isupabase.stroage.entity.table.StorageObjectTableDef.STORAGE_OBJECT;

/**
 * @author yanfeiwuji
 * @date 2024/6/18 18:51
 */
@RequestMapping(StorageStrPool.STORAGE_PATH + "/object")
@RestController
@RequiredArgsConstructor
public class StorageObjectAction {
    private static final Pattern KEY_PATTERN = Pattern.compile("^([a-zA-Z0-9_]+/)*[a-zA-Z0-9_]+$");
    private final S3Provider<Resource> s3Provider;
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    private final BucketService bucketService;
    private final StorageObjectMapper storageObjectMapper;
    private final PgrstDb pgrstDb;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    @Value("${isupabase.storage-update-signed-jwt-exp}")
    private Long storageUpdateSignedJwtExp;


    @GetMapping("{bucketId}/**")
    public ResponseEntity<Resource> download(@PathVariable String bucketId, HttpServletRequest request,
                                             HttpServletResponse response) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);
        final String key = pickKey(bucketId, request);

        final StorageObject storageObject = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));
        if (Objects.isNull(storageObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }
        return readFile(bucket, storageObject, null);
    }

    @GetMapping("sign/{bucketId}/**")
    public ResponseEntity<Resource> signDownload(@PathVariable String bucketId, @RequestParam String token,
                                                 @RequestParam(required = false) String download,
                                                 HttpServletRequest request, HttpServletResponse response) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);
        final String key = pickKey(bucketId, request);
        validToken(token, bucketId, key);

        final StorageObject storageObject = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));
        if (Objects.isNull(storageObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }
        return readFile(bucket, storageObject, download);


    }

    @GetMapping("public/{bucketId}/**")
    public ResponseEntity<Resource> publicDownload(@PathVariable String bucketId, HttpServletRequest request, HttpServletResponse response,
                                                   @RequestParam(required = false) String download
    ) {
        final Bucket bucket = bucketService.nNPublicBucketById(bucketId);
        final String key = pickKey(bucketId, request);
        final StorageObject storageObject = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));
        if (Objects.isNull(storageObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }
        return readFile(bucket, storageObject, download);
    }

    @PostMapping("/{bucketId}/**")
    @Transactional
    public StorageShortInfo upload(@PathVariable String bucketId, HttpServletRequest request
    ) {


        final Bucket bucket = bucketService.nNBucketById(bucketId);

        final String key = pickKey(bucketId, request);


        final long count = storageObjectMapper.selectCountByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));

        if (count > 0) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }
        final StorageObject storageObject = new StorageObject();
        storageObject.setBucketId(bucket.getId());
        storageObject.setName(key);
        storageObject.setOwner(AuthUtils.uid().orElse(-1L));
        storageObject.setOwnerId(AuthUtils.uid().orElse(-1L).toString());
        storageObject.setLastAccessedAt(OffsetDateTime.now());
        storageObject.setPathTokens(CharSequenceUtil.split(key, StrPool.SLASH));

        final ObjectMetadata objectMetadata = uploadFile(bucket, storageObject, request);
        storageObject.setMetadata(objectMetadata);

        pgrstDb.insertSelective(storageObjectMapper, storageObject);


        return new StorageShortInfo(String.valueOf(storageObject.getId()), key);
    }


    @Transactional
    @PostMapping("/upload/sign/{bucketId}/**")
    public SignedUploadVo createSignedUploadUrl(@PathVariable String bucketId, HttpServletRequest request) {
        Bucket bucket = bucketService.nNBucketById(bucketId);
        final String key = pickKey(bucketId, request);
        final StorageObject storageObject =
                storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId()).and(STORAGE_OBJECT.NAME.eq(key)));

        if (Objects.nonNull(storageObject)) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }
        Instant now = Instant.now();
        final String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder()
                .claim(StorageStrPool.TOKEN_KEY_OWNER, AuthUtils.uid().orElse(-1L))
                .claim(StorageStrPool.TOKEN_KEY_URL, bucketId + StrPool.SLASH + key)
                .claim(StorageStrPool.TOKEN_KEY_UPSERT, false)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(storageUpdateSignedJwtExp))
                .build())).getTokenValue();

        return new SignedUploadVo(tokenValue, StorageStrPool.UPLOAD_SIGNED_URL_TEMP.formatted(bucketId, key, tokenValue));
    }

    @Transactional
    @PutMapping("/upload/sign/{bucketId}/**")
    public KeyVo uploadToSignedUrl(@PathVariable String bucketId,
                                   @RequestParam String token,
                                   HttpServletRequest request) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);

        final String key = pickKey(bucketId, request);
        final Jwt jwt = validToken(token, bucketId, key);

        final StorageObject dbStorage = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId()).and(STORAGE_OBJECT.NAME.eq(key)));

        if (Objects.nonNull(dbStorage)) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }


        final Long owner = Optional.ofNullable(jwt.getClaim(StorageStrPool.TOKEN_KEY_OWNER))
                .map(StrUtil::toString)
                .filter(NumberUtil::isNumber)
                .map(NumberUtil::parseLong).orElse(-1L);

        final StorageObject storageObject = new StorageObject();

        storageObject.setBucketId(bucket.getId());
        storageObject.setName(key);
        storageObject.setOwner(owner);
        storageObject.setOwnerId(owner.toString());
        storageObject.setPathTokens(CharSequenceUtil.split(key, StrPool.SLASH));
        storageObject.setLastAccessedAt(OffsetDateTime.now());

        final ObjectMetadata objectMetadata = uploadFile(bucket, storageObject, request);
        storageObject.setMetadata(objectMetadata);


        pgrstDb.insertSelective(storageObjectMapper, storageObject);
        return new KeyVo(bucketId + StrPool.SLASH + key);
    }

    @PostMapping("/sign/{bucketId}/**")
    public SignedVo createSignedUrl(@PathVariable String bucketId, @RequestBody CreateSignedUrlParam param, HttpServletRequest request) {
        Bucket bucket = bucketService.nNBucketById(bucketId);

        final Long expIn = Optional.ofNullable(param).map(CreateSignedUrlParam::expiresIn)
                .filter(it -> it >= 1).orElseThrow(StorageExFactory::createSignedExpInMustGeOne);

        final String key = pickKey(bucketId, request);

        final StorageObject storageObject =
                pgrstDb.selectOneByCondition(storageObjectMapper, STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId()).and(STORAGE_OBJECT.NAME.eq(key)));

        if (Objects.isNull(storageObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }
        return new SignedVo(signURL(storageObject, bucketId, expIn));
    }

    @PostMapping("/sign/{bucketId}")
    public List<SignedPathsVo> createSignedUrls(@PathVariable String bucketId,
                                                @RequestBody CreateSignedUrlsParam param) {
        Bucket bucket = bucketService.nNBucketById(bucketId);


        final Long expIn = Optional.ofNullable(param).map(CreateSignedUrlsParam::expiresIn)
                .filter(it -> it >= 1).orElseThrow(StorageExFactory::createSignedExpInMustGeOne);

        if (Objects.isNull(param.paths())) {
            return List.of(SignedPathsVo.EMPTY_NOT_EXIST);
        }
        if (param.paths().isEmpty()) {
            throw StorageExFactory.CREATE_SIGNED_PATHS_MUST_GE_ONE;
        }

        final Map<String, StorageObject> pathStorageObject = pgrstDb.selectListByCondition(storageObjectMapper, STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId()).and(STORAGE_OBJECT.NAME.in(param.paths())))
                .stream().collect(Collectors.toMap(StorageObject::getName, it -> it));
        return param.paths().stream().map(path -> Optional.ofNullable(pathStorageObject.get(path))
                .map(it -> new SignedPathsVo(null, path, signURL(it, bucketId, expIn)))
                .orElseGet(() -> SignedPathsVo.ofNotExist(path))).toList();


    }


    @PostMapping("/list/{bucketId}")
    @Transactional
    public List<StorageVo> list(@PathVariable String bucketId,
                                @RequestBody ObjectListParam param
    ) {
        final QueryWrapper queryWrapper = param.toQueryWrapper(bucketId);
        final List<StorageObject> storageObjects = pgrstDb.selectListByQuery(storageObjectMapper, queryWrapper);

        final List<Long> ids = storageObjects.stream().map(StorageObject::getId).toList();
        if (ids.isEmpty()) {
            return List.of();
        }
        final Map<String, StorageVo> info = storageObjectMapper.selectListByIds(ids)
                .stream().map(it -> StorageVo.of(it, param.prefix()))
                .collect(Collectors.toMap(StorageVo::getId, it -> it));

        return ids.stream().map(String::valueOf).map(info::get)
                .peek(it -> {
                    if (it.isDir()) {
                        it.setId(null);
                    }
                })
                .toList();
    }

    @PutMapping("/{bucketId}/**")
    @Transactional
    public StorageShortInfo update(@PathVariable String bucketId, HttpServletRequest request) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);

        final String key = pickKey(bucketId, request);
        final StorageObject dbStorageObject = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.NAME.eq(key).and(STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId())));
        if (Objects.isNull(dbStorageObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }

        dbStorageObject.setOwner(AuthUtils.uid().orElse(-1L));
        dbStorageObject.setOwnerId(AuthUtils.uid().orElse(-1L).toString());
        dbStorageObject.setLastAccessedAt(OffsetDateTime.now());
        final ObjectMetadata objectMetadata = uploadFile(bucket, dbStorageObject, request);
        dbStorageObject.setMetadata(objectMetadata);

        pgrstDb.update(storageObjectMapper, dbStorageObject);

        return new StorageShortInfo(String.valueOf(dbStorageObject.getId()), key);
    }

    @PostMapping("/move")
    @Transactional
    public StorageMessage move(@RequestBody ObjectMoveOrCopyParam objectMoveParam) {
        final String bucketId = objectMoveParam.getBucketId();
        final String sourceKey = objectMoveParam.getSourceKey();
        final String destinationKey = objectMoveParam.getDestinationKey();
        final StorageObject sourceObject =
                pgrstDb.selectOneByCondition(storageObjectMapper, STORAGE_OBJECT.BUCKET_ID.eq(bucketId).and(STORAGE_OBJECT.NAME.eq(sourceKey)));

        if (Objects.isNull(sourceObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }
        final StorageObject destinationObject = storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.BUCKET_ID.eq(bucketId).and(STORAGE_OBJECT.NAME.eq(destinationKey)));

        if (Objects.nonNull(destinationObject)) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }
        sourceObject.setName(objectMoveParam.getDestinationKey());
        sourceObject.setPathTokens(CharSequenceUtil.split(objectMoveParam.getDestinationKey(), StrPool.SLASH));
        pgrstDb.update(storageObjectMapper, sourceObject);
        return StorageMessage.SUCCESS_MOVED;
    }

    @PostMapping("/copy")
    @Transactional
    public KeyVo copy(@RequestBody ObjectMoveOrCopyParam objectMoveParam) {
        final String bucketId = objectMoveParam.getBucketId();
        final String sourceKey = objectMoveParam.getSourceKey();
        final String destinationKey = objectMoveParam.getDestinationKey();
        final StorageObject sourceObject =
                pgrstDb.selectOneByCondition(storageObjectMapper, STORAGE_OBJECT.BUCKET_ID.eq(bucketId).and(STORAGE_OBJECT.NAME.eq(sourceKey)));

        if (Objects.isNull(sourceObject)) {
            throw StorageExFactory.OBJECT_NOT_FOUND;
        }
        final StorageObject destinationObject =
                storageObjectMapper.selectOneByCondition(STORAGE_OBJECT.BUCKET_ID.eq(bucketId).and(STORAGE_OBJECT.NAME.eq(destinationKey)));

        if (Objects.nonNull(destinationObject)) {
            throw StorageExFactory.RESOURCE_ALREADY_EXIST;
        }
        sourceObject.setId(null);
        sourceObject.setName(destinationKey);
        sourceObject.setPathTokens(CharSequenceUtil.split(destinationKey, StrPool.SLASH));
        pgrstDb.insertSelective(storageObjectMapper, sourceObject);

        return new KeyVo(sourceObject.getBucketId() + StrPool.SLASH + destinationKey);
    }


    @DeleteMapping("{bucketId}")
    @Transactional
    public List<StorageObject> remove(@PathVariable String bucketId, @RequestBody ObjectDeleteParam objectDeleteParam) {
        final Bucket bucket = bucketService.nNBucketById(bucketId);
        final List<String> prefixes = objectDeleteParam.getPrefixes();
        if (Objects.isNull(prefixes)) {
            return List.of();
        }
        if (prefixes.isEmpty()) {
            throw StorageExFactory.DELETE_PREFIXES_IS_EMPTY;
        }
        return pgrstDb.deleteByCondition(storageObjectMapper, STORAGE_OBJECT.BUCKET_ID.eq(bucket.getId()).and(STORAGE_OBJECT.NAME.in(prefixes)));
    }


    private String pickKey(String bucketId, HttpServletRequest request) {
        String key = Optional.ofNullable(request).map(HttpServletRequest::getServletPath)
                .map(it -> it.split(bucketId, 2))
                .filter(it -> it.length >= 2)
                .map(it -> it[1])
                .map(it -> CharSequenceUtil.removePrefix(it, StrPool.SLASH))
                .orElse(CharSequenceUtil.EMPTY);

        if (key.equals(CharSequenceUtil.EMPTY) || !KEY_PATTERN.matcher(key).matches()) {
            throw StorageExFactory.invalidKey(key).get();
        }
        return key;
    }

    private String signURL(StorageObject storageObject, String bucketId, Long expIn) {
        Instant now = Instant.now();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(JwtClaimsSet.builder()
                .claim(StorageStrPool.TOKEN_KEY_URL, bucketId + StrPool.SLASH + storageObject.getName())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expIn))
                .build())).getTokenValue();
        return StorageStrPool.SIGNED_URL_TEMP.formatted(bucketId, storageObject.getName(), tokenValue);
    }

    private Jwt validToken(String token, String bucketId, String key) {

        try {
            Jwt jwt = jwtDecoder.decode(token);
            final Instant expiresAt = jwt.getExpiresAt();
            // token
            if (Objects.isNull(expiresAt) || expiresAt.isBefore(Instant.now())) {
                // todo get info from supabase
                throw StorageExFactory.INVALID_JWT;
            }

            Optional.ofNullable(jwt.getClaim(StorageStrPool.TOKEN_KEY_URL))
                    .map(StrUtil::toString)
                    .map(it -> CharSequenceUtil.split(it, CharPool.SLASH, 2))
                    .filter(it -> it.size() == 2)
                    .filter(it -> CharSequenceUtil.equals(bucketId, it.getFirst()) && CharSequenceUtil.equals(key, it.get(1)))
                    .orElseThrow(StorageExFactory::invalidSignature);
            return jwt;
        } catch (Exception e) {
            throw StorageExFactory.INVALID_JWT;
        }
    }

    @SneakyThrows
    private ObjectMetadata uploadFile(Bucket bucket, StorageObject storageObject, HttpServletRequest request) {
        final MultipartFile multipartFile = Optional.of(request)
                .filter(MultipartHttpServletRequest.class::isInstance)
                .map(MultipartHttpServletRequest.class::cast)
                .map(it -> it.getFile(CharSequenceUtil.EMPTY))
                .orElseThrow(StorageExFactory::uploadNullError);


        final long size = multipartFile.getSize();
        final Long bucketAllowSize = Optional.ofNullable(bucket.getFileSizeLimit()).orElse(0L);

        if (size >= bucketAllowSize) {
            throw StorageExFactory.PAYLOAD_TOO_LAGER;
        }

        final MimeType contextMimeType =
                Optional.ofNullable(multipartFile.getContentType())
                        .map(MimeType::valueOf).orElse(MimeTypeUtils.TEXT_PLAIN);

        final List<String> allowedMimeTypes = bucket.getAllowedMimeTypes();
        if (Objects.nonNull(allowedMimeTypes) && !allowedMimeTypes.isEmpty()) {
            final boolean match = allowedMimeTypes.stream().map(MimeType::valueOf).anyMatch(it -> it.includes(contextMimeType));
            if (!match) {
                throw StorageExFactory.invalidMimeType(contextMimeType.toString()).get();
            }
        }


        final String cacheControl = request.getParameter("cacheControl");

        final byte[] bytes = multipartFile.getBytes();
        final String name = storageObject.getName();
        final String contentType = multipartFile.getContentType();
        // todo  filter handler file size and minetype
        return s3Provider.putObject(bucket, name, cacheControl, contentType, bytes);
    }

    private ResponseEntity<Resource> readFile(Bucket bucket, StorageObject storageObject, String download) {
        return s3Provider.downloadObject(bucket, storageObject.getName(), download, storageObject.getMetadata());

    }


}
