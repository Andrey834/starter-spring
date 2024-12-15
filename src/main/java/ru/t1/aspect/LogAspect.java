package ru.t1.aspect;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.t1.prop.WebLogProperties;

import java.time.LocalDateTime;
import java.util.Arrays;

@Aspect
@Slf4j(topic = "LoggingAspect")
public class LogAspect {
    private final WebLogProperties webLogProperties;

    public LogAspect(WebLogProperties webLogProperties) {
        this.webLogProperties = webLogProperties;
    }

    @Around("@annotation(ru.t1.annotation.WebLog)")
    public Object webLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        boolean webLogEnabled = webLogProperties.isEnabled();

        if (webLogEnabled) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
                    .currentRequestAttributes();

            HttpServletRequest request = servletRequestAttributes.getRequest();
            HttpServletResponse response = servletRequestAttributes.getResponse();

            Object proceed = joinPoint.proceed();
            String detailing = String.valueOf(webLogProperties.getDetailing());

            try {
                return proceed;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                String message = getLogMessage(detailing, request, response, proceed, start);
                log.info(message);
            }
        }

        return joinPoint.proceed();
    }

    @AfterThrowing(
            pointcut = "@annotation(ru.t1.annotation.ExceptionLog)",
            throwing = "exception")
    public void exceptionLog(JoinPoint joinPoint, Throwable exception) {
        Signature signature = joinPoint.getSignature();
        String declaringType = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.info("ОШИБКА: Метод -> {}(), Класс -> {}, Причина -> {}", methodName, declaringType,
                exception.getMessage());
    }

    @AfterReturning(
            pointcut = "@annotation(ru.t1.annotation.LogReturningObject)",
            returning = "result")
    public void afterReturningObjectLog(JoinPoint joinPoint, Object result) {
        Signature signature = joinPoint.getSignature();
        String declaringType = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        log.info("ВЫЗОВ: Метод {}(), Класс {}, Объект -> {}", methodName, declaringType, result);
    }

    @Around("@annotation(ru.t1.annotation.TimeTracking)")
    public Object executionTimeLog(ProceedingJoinPoint joinPoint) {
        long start = System.currentTimeMillis();

        Signature signature = joinPoint.getSignature();
        String declaringType = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException("TimeTracking Exception", e);
        } finally {
            long end = System.currentTimeMillis();
            long duration = end - start;

            log.info("ВРЕМЯ: Метод {}(), Аргументы {}, Класс {}: Время выполнения -> {}мс",
                    methodName, args, declaringType, duration
            );
        }
    }

    private String getLogMessage(
            String detailing,
            HttpServletRequest request,
            HttpServletResponse response,
            Object proceed,
            long start
    ) {
        long end = System.currentTimeMillis();
        long duration = end - start;

        switch (detailing) {
            case "SHORT" -> {
                return String.format("request method: %s, " +
                                     "request URI: %s, " +
                                     "response status: %d",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus()
                );
            }
            case "FULL" -> {
                StringBuilder params = new StringBuilder();

                request.getParameterMap().forEach((key, value) -> params
                        .append(key)
                        .append("=")
                        .append(Arrays.toString(value))
                        .append(";")
                );

                return String.format("date: %s, " +
                                     "request method: %s, " +
                                     "request URI: %s, " +
                                     "params: %s, " +
                                     "from: %s, " +
                                     "response status: %d, " +
                                     "body: %s, " +
                                     "request processing time: %d ms",
                        LocalDateTime.now(),
                        request.getMethod(),
                        request.getRequestURI(),
                        params,
                        request.getRemoteAddr(),
                        response.getStatus(),
                        proceed.toString(),
                        duration
                );
            }
            default -> {
                return String.format("request method: %s, " +
                                     "request URI: %s, " +
                                     "response status: %d, " +
                                     "request processing time: %d ms",
                        request.getMethod(),
                        request.getRequestURI(),
                        response.getStatus(),
                        duration
                );
            }
        }
    }
}
