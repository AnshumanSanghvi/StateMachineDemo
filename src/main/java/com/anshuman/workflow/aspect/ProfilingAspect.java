package com.anshuman.workflow.aspect;

import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ProfilingAspect {

    @Around("this(org.springframework.data.jpa.repository.JpaRepository)")
    public Object databaseCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        return loggingMethod(joinPoint, "DB Call");
    }

    @Around("@annotation(com.anshuman.workflow.annotation.Profile)")
    public Object annotatedMethodCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        return loggingMethod(joinPoint, "Annotated Method");
    }

    @Around("execution(public * com.anshuman.workflow.resource.*.*(..))")
    public Object restControllerCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        return loggingMethod(joinPoint, "Rest API Call");
    }

    public static Object loggingMethod(ProceedingJoinPoint joinPoint, String source) throws Throwable {
        final String methodName = joinPoint.getSignature().toShortString();
        final double startTime = Instant.now().toEpochMilli();
        double duration;
        final Object returnValue;
        String message = "PROFILING {}: method={} completed in {} seconds";
        try {
            returnValue = joinPoint.proceed();
        } catch (Throwable ex) {
            duration = (Instant.now().toEpochMilli() - startTime) / 1000;
            if (duration > 5)
                log.info(message, source, methodName, String.format("%.3f", duration));
            else
                log.debug(message, source, methodName, String.format("%.3f", duration));

            throw ex;
        }
        duration = (Instant.now().toEpochMilli() - startTime) / 1000;
        if (duration > 5)
            log.info(message, source, methodName, String.format("%.3f", duration));
        else
            log.debug( message, source, methodName, String.format("%.3f", duration));
        return returnValue;
    }

}
