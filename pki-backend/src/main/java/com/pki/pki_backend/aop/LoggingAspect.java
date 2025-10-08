package com.pki.pki_backend.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    // Definišemo "pointcut" koji obuhvata sve metode u servisima i kontrolerima
    @Pointcut("within(com.pki.pki_backend.service..*) || within(com.pki.pki_backend.controller..*)")
    public void applicationPackagePointcut() {
        // Metoda je prazna jer služi samo kao nosilac @Pointcut anotacije
    }

    // Izvršava se pre poziva metode
    @Before("applicationPackagePointcut()")
    public void logBefore(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";

        // AŽURIRANO: Koristimo getSimpleName() da dobijemo samo ime klase
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("User: '{}' is starting execution of method: '{}.{}'", user, className, methodName);
    }

    // Izvršava se nakon uspešnog završetka metode
    @AfterReturning("applicationPackagePointcut()")
    public void logAfterReturning(JoinPoint joinPoint) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";

        // AŽURIRANO: Koristimo getSimpleName() da dobijemo samo ime klase
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("User: '{}' successfully executed method: '{}.{}'", user, className, methodName);
    }

    // Izvršava se ako metoda baci izuzetak (exception)
    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "e")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable e) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = (authentication != null) ? authentication.getName() : "anonymous";

        // AŽURIRANO: Koristimo getSimpleName() i izbacujemo stack trace, logujemo samo poruku greške
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.error("User: '{}' encountered an error in method: '{}.{}' | Error: {}", user, className, methodName, e.getMessage());
    }
}

