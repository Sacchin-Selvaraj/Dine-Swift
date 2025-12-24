package com.dineswift.userservice.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around(value = "within(com.dineswift.userservice.service..*)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("Method execution started: {}", methodName);
        long startTime = System.currentTimeMillis();
        log.info("Method arguments: {}", args != null ? java.util.Arrays.toString(args) : "[]");
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Method execution completed: {} with duration: {}", methodName,duration);
            return result;
        } catch (Exception ex) {
            log.error("Method execution failed: {}. Exception: {}", methodName, ex.getMessage());
            throw ex;
        }
    }

    @Around(value = "within(com.dineswift.userservice.controller..*)")
    public Object logControllerMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        log.info("Controller method execution started: {}", methodName);
        long startTime = System.currentTimeMillis();
        log.info("Controller method arguments: {}", args != null ? java.util.Arrays.toString(args) : "[]");
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Controller method execution completed: {} with duration: {}", methodName,duration);
            return result;
        } catch (Exception ex) {
            log.error("Controller method execution failed: {}. Exception: {}", methodName, ex.getMessage());
            throw ex;
        }
    }
}
