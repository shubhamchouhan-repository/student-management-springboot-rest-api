package com.example.sms.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Cross-cutting concern: how long each service method takes, logged
 * without a single line of timing code inside StudentServiceImpl,
 * CourseServiceImpl, etc. That's the whole point of AOP — this one
 * class adds timing to every service method in the project at once.
 *
 * Pointcut target: com.batch.sms.service.*.*(..) — one level under
 * "service", which is the SERVICE INTERFACES (StudentService,
 * CourseService, DepartmentService, EnrollmentRequestService,
 * FileStorageService), not service.impl. This matters: these services
 * are proxied via JDK dynamic proxies (they implement interfaces), so
 * the join point Spring AOP actually intercepts is the INTERFACE
 * method, not the impl class's method. Pointing this at
 * com.batch.sms.service.impl.*.*(..) instead would silently match
 * nothing.
 *
 * No @EnableAspectJAutoProxy needed — Spring Boot auto-configures AOP
 * proxying the moment spring-boot-starter-aop is on the classpath
 * (spring.aop.auto=true is the default).
 */

@Aspect
@Component
public class ExecutionTimeLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ExecutionTimeLoggingAspect.class);

    @Around("execution(* com.batch.sms.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("[AOP] {} args={} executed in {} ms", signature, Arrays.toString(joinPoint.getArgs()), duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[AOP] {} args={} threw {} after {} ms",
                    signature, Arrays.toString(joinPoint.getArgs()), ex.getClass().getSimpleName(), duration);
            // Re-throw: the aspect only OBSERVES the call, it must never
            // swallow the exception — the real exception handling still
            // happens in GlobalExceptionHandler, same as before.
            throw ex;
        }
    }
}
