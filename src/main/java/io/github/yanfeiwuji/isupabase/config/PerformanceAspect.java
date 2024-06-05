package io.github.yanfeiwuji.isupabase.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @author yanfeiwuji
 * @date 2024/6/5 10:12
 */
@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    @Around("@annotation(TrackExecutionTime)")
    public Object trackTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object obj = joinPoint.proceed();
        long endTime = System.currentTimeMillis();
        log.info("Method {} executed in {} ms", joinPoint.getSignature().toString(), endTime - startTime);
        return obj;
    }
}
