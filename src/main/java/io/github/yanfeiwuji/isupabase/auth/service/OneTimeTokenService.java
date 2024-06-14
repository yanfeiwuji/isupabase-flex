package io.github.yanfeiwuji.isupabase.auth.service;

import cn.hutool.core.lang.id.NanoId;
import io.github.yanfeiwuji.isupabase.auth.entity.ETokenType;
import io.github.yanfeiwuji.isupabase.auth.entity.OneTimeToken;
import io.github.yanfeiwuji.isupabase.auth.entity.User;
import io.github.yanfeiwuji.isupabase.auth.mapper.OneTimeTokenMapper;
import io.github.yanfeiwuji.isupabase.config.ISupabaseProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
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
    private final OneTimeTokenMapper oneTimeTokenMapper;

    private final ISupabaseProperties iSupabaseProperties;
    private Long oneTimeExpiredMinutes;

    @PostConstruct
    public void init() {
        oneTimeExpiredMinutes = iSupabaseProperties.getOneTimeExpiredMinutes();

    }

    public OneTimeToken recoverToken(User user) {
        final OneTimeToken oneTimeToken = new OneTimeToken();
        oneTimeToken.setTokenType(ETokenType.RECOVERY_TOKEN);
        oneTimeToken.setUserId(user.getId());
        oneTimeToken.setTokenHash(NanoId.randomNanoId());
        oneTimeToken.setRelatesTo(user.getEmail());
        oneTimeTokenMapper.insert(oneTimeToken);
        return oneTimeToken;
    }

    public Optional<OneTimeToken> verifyToken(String tokenHash, ETokenType tokenType) {
        final Optional<OneTimeToken> oneTimeTokenOptional = Optional.ofNullable(tokenHash)
                .map(it -> ONE_TIME_TOKEN.TOKEN_HASH.eq(it).and(ONE_TIME_TOKEN.TOKEN_TYPE.eq(tokenType)))
                .map(oneTimeTokenMapper::selectOneByCondition)
                .filter(it -> it.getCreatedAt().plusMinutes(oneTimeExpiredMinutes).isAfter(OffsetDateTime.now()));
        oneTimeTokenOptional.ifPresent(oneTimeTokenMapper::delete);
        return oneTimeTokenOptional;
    }


}
