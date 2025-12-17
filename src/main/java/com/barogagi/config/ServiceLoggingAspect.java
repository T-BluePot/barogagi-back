package com.barogagi.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServiceLoggingAspect {

    @Around("execution(* com.example..service..*(..))")
    public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();

        log.info("Service Start - {}.{}", className, methodName);

        try {
            Object result = joinPoint.proceed();

            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Service End - {}.{}, time={}ms",
                    className, methodName, elapsedTime);

            return result;

        } catch (Exception e) {
            log.error("Service Exception - {}.{}", className, methodName, e);
            throw e;
        }
    }
}

