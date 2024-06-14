package io.github.yanfeiwuji.isupabase.auth.service;

import io.github.yanfeiwuji.isupabase.auth.mapper.RefreshTokenMapper;
import io.github.yanfeiwuji.isupabase.auth.mapper.SessionMapper;
import io.github.yanfeiwuji.isupabase.auth.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static io.github.yanfeiwuji.isupabase.auth.entity.table.RefreshTokenTableDef.REFRESH_TOKEN;
import static io.github.yanfeiwuji.isupabase.auth.entity.table.SessionTableDef.SESSION;

/**
 * @author yanfeiwuji
 * @date 2024/6/13 10:32
 */
@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionMapper sessionMapper;
    private final RefreshTokenMapper refreshTokenMapper;

    public void logoutGlobal() {
        final Optional<Long> uid = AuthUtil.uid();
        if (uid.isEmpty()) {
            return;
        }
        sessionMapper.deleteByCondition(SESSION.USER_ID.eq(uid.get()));
        refreshTokenMapper.deleteByCondition(REFRESH_TOKEN.USER_ID.eq(uid.get()));
    }

    public void logoutLocal() {
        final Optional<Long> sessionId = AuthUtil.sessionId();

        if (sessionId.isEmpty()) {
            return;
        }

        sessionMapper.deleteById(sessionId.get());
        refreshTokenMapper.deleteByCondition(REFRESH_TOKEN.SESSION_ID.eq(sessionId.get()));
    }

}
