package io.github.yanfeiwuji.isupabase.auth.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import io.github.yanfeiwuji.isupabase.constants.PgrstStrPool;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 17:54
 */
@UtilityClass
public class ServletUtils {
    private String REST_PATH_PRE = StrPool.SLASH + PgrstStrPool.REST_PATH + StrPool.SLASH;
    public String userAgent() {

        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(req -> req.getHeader(AuthStrPool.USER_AGENT_KEY)).orElse(CharSequenceUtil.EMPTY);
    }

    public String ip() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest).map(ServletRequest::getRemoteAddr)
                .orElse(CharSequenceUtil.EMPTY);
    }

    public String origin() {

        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(it -> it.getHeader(AuthStrPool.REFERER_HEADER_KEY))
                .orElse(CharSequenceUtil.EMPTY);
    }

    public String tableName() {

        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getServletPath)
                .map(it -> CharSequenceUtil.removePrefix(it, REST_PATH_PRE))
                .orElse(CharSequenceUtil.EMPTY);
    }

}
