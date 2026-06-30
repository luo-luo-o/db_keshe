package pers.luoluo.databasekeshe.logging.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import pers.luoluo.databasekeshe.logging.RequestLogContext;

@Component
public class RuntimeLogInterceptor implements HandlerInterceptor {

    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger(RequestLogContext.ACCESS_LOGGER_NAME);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(RequestLogContext.START_TIME_ATTRIBUTE, System.currentTimeMillis());

        String requestId = RequestLogContext.normalizeHeaderValue(request.getHeader(RequestLogContext.REQUEST_ID_HEADER));
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }

        response.setHeader(RequestLogContext.REQUEST_ID_HEADER, requestId);
        MDC.put(RequestLogContext.REQUEST_ID_MDC_KEY, requestId);
        putMdcIfPresent(RequestLogContext.USER_ID_MDC_KEY, request.getHeader(RequestLogContext.USER_ID_HEADER));
        putMdcIfPresent(RequestLogContext.ROLE_CODE_MDC_KEY, request.getHeader(RequestLogContext.ROLE_CODE_HEADER));
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception exception
    ) {
        try {
            if (request.getRequestURI().startsWith("/api/")) {
                ACCESS_LOGGER.info(buildAccessLogLine(request, response));
            }
        } finally {
            MDC.remove(RequestLogContext.REQUEST_ID_MDC_KEY);
            MDC.remove(RequestLogContext.USER_ID_MDC_KEY);
            MDC.remove(RequestLogContext.ROLE_CODE_MDC_KEY);
        }
    }

    private void putMdcIfPresent(String key, String value) {
        String normalizedValue = RequestLogContext.normalizeHeaderValue(value);
        if (normalizedValue == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, normalizedValue);
    }

    private String buildAccessLogLine(HttpServletRequest request, HttpServletResponse response) {
        return "requestId=" + RequestLogContext.sanitizeForLog(MDC.get(RequestLogContext.REQUEST_ID_MDC_KEY))
                + " userId=" + RequestLogContext.sanitizeForLog(MDC.get(RequestLogContext.USER_ID_MDC_KEY))
                + " roleCode=" + RequestLogContext.sanitizeForLog(MDC.get(RequestLogContext.ROLE_CODE_MDC_KEY))
                + " method=" + RequestLogContext.sanitizeForLog(request.getMethod())
                + " path=" + RequestLogContext.sanitizeForLog(request.getRequestURI())
                + " queryString=" + RequestLogContext.sanitizeForLog(request.getQueryString())
                + " status=" + response.getStatus()
                + " elapsedMs=" + elapsedMillis(request)
                + " remoteAddr=" + RequestLogContext.sanitizeForLog(resolveRemoteAddress(request));
    }

    private long elapsedMillis(HttpServletRequest request) {
        Object startTime = request.getAttribute(RequestLogContext.START_TIME_ATTRIBUTE);
        if (startTime instanceof Long value) {
            return System.currentTimeMillis() - value;
        }
        return 0;
    }

    private String resolveRemoteAddress(HttpServletRequest request) {
        String forwardedFor = RequestLogContext.normalizeHeaderValue(request.getHeader("X-Forwarded-For"));
        if (forwardedFor != null) {
            int separator = forwardedFor.indexOf(',');
            return separator >= 0 ? forwardedFor.substring(0, separator).trim() : forwardedFor;
        }
        return request.getRemoteAddr();
    }
}
