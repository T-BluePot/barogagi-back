package com.barogagi.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("Controller Start - {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();
            log.info("Controller End - {}.{}", className, methodName);
            return result;
        } catch (Exception e) {
            log.error("Controller Exception - {}.{}", className, methodName, e);
            throw e;
        }
    }
}
