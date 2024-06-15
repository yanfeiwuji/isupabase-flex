package io.github.yanfeiwuji.isupabase.auth.utils;

import cn.hutool.core.text.CharSequenceUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 17:54
 */
@UtilityClass
public class ServletUtils {
    public String userAgent() {

        return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(req -> req.getHeader(AuthStrPool.USER_AGENT_KEY)).orElse(CharSequenceUtil.EMPTY);
    }

    public String ip() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest).map(ServletRequest::getRemoteAddr)
                .orElse(CharSequenceUtil.EMPTY);
    }

    public String origin() {
       
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes()).filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(it -> it.getHeader(AuthStrPool.REFERER_HEADER_KEY))
                .orElse(CharSequenceUtil.EMPTY);
    }

}
