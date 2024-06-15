package io.github.yanfeiwuji.isupabase.auth.service;

import cn.hutool.core.lang.id.NanoId;
import io.github.yanfeiwuji.isupabase.auth.entity.ETokenType;
import io.github.yanfeiwuji.isupabase.auth.entity.OneTimeToken;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.mapper.OneTimeTokenMapper;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.OneTimeTokenTableDef.ONE_TIME_TOKEN;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 20:45
 */
@Service
@RequiredArgsConstructor
public class OneTimeTokenService {
    private static final Logger log = LoggerFactory.getLogger(OneTimeTokenService.class);
    private final OneTimeTokenMapper oneTimeTokenMapper;

    private final ISupabaseProperties iSupabaseProperties;
    private Long oneTimeExpiredMinutes;

    @PostConstruct
    public void init() {
        oneTimeExpiredMinutes = iSupabaseProperties.getOneTimeExpiredMinutes();

    }

    public OneTimeToken recoverToken(User user) {
        return createToken(user, ETokenType.RECOVERY_TOKEN);
    }

    public OneTimeToken emailChangeTokenCurrent(User user) {
        return createToken(user, user.getEmail(), user.getEmailChangeTokenCurrent(), ETokenType.EMAIL_CHANGE_TOKEN_CURRENT);
    }

    public OneTimeToken emailChangeTokenNew(User user) {
        return createToken(user, user.getEmailChange(), user.getEmailChangeTokenNew(), ETokenType.EMAIL_CHANGE_TOKEN_NEW);
    }

    private OneTimeToken createToken(User user, ETokenType tokenType) {
        return createToken(user, user.getEmail(), NanoId.randomNanoId(), tokenType);
    }

    private OneTimeToken createToken(User user, String relatesTo, String tokenHash, ETokenType tokenType) {
        // before create remove has one time token
        oneTimeTokenMapper.deleteByCondition(ONE_TIME_TOKEN.TOKEN_TYPE.eq(tokenType).and(ONE_TIME_TOKEN.USER_ID.eq(user.getId())));

        final OneTimeToken oneTimeToken = new OneTimeToken();
        oneTimeToken.setTokenType(tokenType);
        oneTimeToken.setUserId(user.getId());
        oneTimeToken.setTokenHash(tokenHash);
        oneTimeToken.setRelatesTo(relatesTo);
        oneTimeTokenMapper.insert(oneTimeToken);
        return oneTimeToken;
    }


    public Optional<OneTimeToken> verifyToken(String tokenHash, ETokenType tokenType) {
        final Optional<OneTimeToken> oneTimeTokenOptional = Optional.ofNullable(tokenHash)
                .map(it -> ONE_TIME_TOKEN.TOKEN_HASH.eq(it).and(ONE_TIME_TOKEN.TOKEN_TYPE.eq(tokenType)))
                .map(oneTimeTokenMapper::selectOneByCondition);
        // has then delete
        oneTimeTokenOptional.ifPresent(oneTimeTokenMapper::delete);
        return oneTimeTokenOptional.filter(it -> it.getCreatedAt().plusMinutes(oneTimeExpiredMinutes).isAfter(OffsetDateTime.now()));
    }

    public Optional<OneTimeToken> verifyToken(String tokenHash) {
        final Optional<OneTimeToken> oneTimeTokenOptional = Optional.ofNullable(tokenHash)
                .map(ONE_TIME_TOKEN.TOKEN_HASH::eq)
                .map(oneTimeTokenMapper::selectOneByCondition);
        oneTimeTokenOptional.ifPresent(oneTimeTokenMapper::delete);
        return oneTimeTokenOptional.filter(it -> it.getCreatedAt().plusMinutes(oneTimeExpiredMinutes).isAfter(OffsetDateTime.now()));
    }


}
