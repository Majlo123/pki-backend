package com.pki.pki_backend.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // Definišemo "pointcut" - koje metode želimo da pratimo
    // Ovde pratimo sve javne metode unutar CertificateService i UserService
    private final String servicePointcut = "execution(public * com.pki.pki_backend.service.CertificateService.*(..)) || " +
            "execution(public * com.pki.pki_backend.service.UserService.createCaUser(..))";

    @Before(servicePointcut)
    public void logBefore(JoinPoint joinPoint) {
        String user = getUsername();
        logger.info("User: '{}' is starting execution of method: '{}'", user, joinPoint.getSignature().getName());
    }

    @AfterReturning(pointcut = servicePointcut, returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        String user = getUsername();
        logger.info("User: '{}' successfully executed method: '{}'", user, joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = servicePointcut, throwing = "exception")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
        String user = getUsername();
        logger.error("User: '{}' encountered an error in method: '{}'. Exception: {}",
                user, joinPoint.getSignature().getName(), exception.getMessage());
    }

    private String getUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString(); // Vraća npr. "anonymousUser" ako niko nije prijavljen
        }
    }
}
