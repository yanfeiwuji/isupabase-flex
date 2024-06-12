package io.github.yanfeiwuji.isupabase.auth.utils;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import io.github.yanfeiwuji.isupabase.constants.AuthStrPool;
import jakarta.servlet.ServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * @author yanfeiwuji
 * @date 2024/6/12 17:54
 */
@UtilityClass
public class ServletUtil {
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
}
