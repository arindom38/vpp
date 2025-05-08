package com.challenge.vpp.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class AccessLoggingAspect {

    private static final int MAX_LENGTH = 1000;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PostMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.PutMapping)" +
            "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    void requestMapping() {
    }


    @Before("requestMapping()")
    void before(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        String args = Arrays.toString(joinPoint.getArgs());

        log.info("REST request on {}.{}() with {}", signature.getDeclaringTypeName(), signature.getName(), args);
    }

    @AfterReturning(value = "requestMapping()", returning = "returning")
    void afterReturning(JoinPoint joinPoint, Object returning) {
        Signature signature = joinPoint.getSignature();

        String response = null;
        if (returning != null) {
            response = returning.toString();
            if (response.length() > MAX_LENGTH) {
                response = response.substring(0, MAX_LENGTH);
            }
        }
        log.info("REST response on {}.{}() with {}", signature.getDeclaringTypeName(), signature.getName(), response);

    }

    @AfterThrowing(value = "requestMapping()", throwing = "throwing")
    void afterThrowing(JoinPoint joinPoint, Throwable throwing) {
        Signature signature = joinPoint.getSignature();

        log.info("REST error on {}.{}() with {}", signature.getDeclaringTypeName(), signature.getName(), throwing.getMessage());

    }
}
