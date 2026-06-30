package pers.luoluo.databasekeshe.logging.service;

import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RuntimeLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeLogService.class);
    private static final Pattern SENSITIVE_KV_PATTERN = Pattern.compile("(?i)(password|passwd|pwd|token|authorization)=([^\\s,;]+)");
    private static final Pattern JSON_PASSWORD_PATTERN = Pattern.compile("(?i)(\"(?:password|passwd|pwd|token|authorization)\"\\s*:\\s*\")([^\"]*)(\")");

    public void debug(String message, String context) {
        LOGGER.debug("message={} context={}", sanitize(message), sanitize(context));
    }

    public void info(String message, String context) {
        LOGGER.info("message={} context={}", sanitize(message), sanitize(context));
    }

    public void warn(String message, String context) {
        LOGGER.warn("message={} context={}", sanitize(message), sanitize(context));
    }

    public void error(String message, String context) {
        LOGGER.error("message={} context={}", sanitize(message), sanitize(context));
    }

    public void error(String message, String context, Throwable throwable) {
        LOGGER.error("message={} context={}", sanitize(message), sanitize(context), sanitizeThrowable(throwable));
    }

    private String sanitize(String value) {
        if (value == null) {
            return "-";
        }

        String sanitized = value.replace('\r', ' ').replace('\n', ' ').trim();
        sanitized = SENSITIVE_KV_PATTERN.matcher(sanitized).replaceAll("$1=***");
        sanitized = JSON_PASSWORD_PATTERN.matcher(sanitized).replaceAll("$1***$3");
        return sanitized;
    }

    private Throwable sanitizeThrowable(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        RuntimeException sanitized = new RuntimeException(sanitize(throwable.getMessage()));
        sanitized.setStackTrace(throwable.getStackTrace());
        Throwable cause = throwable.getCause();
        if (cause != null && cause != throwable) {
            sanitized.initCause(sanitizeThrowable(cause));
        }
        for (Throwable suppressed : throwable.getSuppressed()) {
            sanitized.addSuppressed(sanitizeThrowable(suppressed));
        }
        return sanitized;
    }
}
