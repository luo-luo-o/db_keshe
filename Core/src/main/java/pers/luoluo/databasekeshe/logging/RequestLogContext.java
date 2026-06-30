package pers.luoluo.databasekeshe.logging;

import java.util.regex.Pattern;

public final class RequestLogContext {

    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ROLE_CODE_HEADER = "X-Role-Code";

    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String USER_ID_MDC_KEY = "userId";
    public static final String ROLE_CODE_MDC_KEY = "roleCode";

    public static final String START_TIME_ATTRIBUTE = RequestLogContext.class.getName() + ".startTime";
    public static final String ACCESS_LOGGER_NAME = "ACCESS_LOG";
    private static final Pattern SENSITIVE_PATTERN = Pattern.compile("(?i)(password|passwd|pwd|token|authorization)=([^&\\s]+)");

    private RequestLogContext() {
    }

    public static String normalizeHeaderValue(String value) {
        if (value == null) {
            return null;
        }

        String sanitized = value
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .trim();
        if (sanitized.isEmpty()) {
            return null;
        }

        return sanitized.length() > 128 ? sanitized.substring(0, 128) : sanitized;
    }

    public static String sanitizeForLog(Object value) {
        if (value == null) {
            return "-";
        }

        String text = normalizeHeaderValue(String.valueOf(value));
        if (text == null) {
            return "-";
        }

        StringBuilder builder = new StringBuilder(text.length());
        boolean previousWasUnderscore = false;
        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (Character.isWhitespace(current) || Character.isISOControl(current)) {
                if (!previousWasUnderscore) {
                    builder.append('_');
                    previousWasUnderscore = true;
                }
                continue;
            }
            builder.append(current);
            previousWasUnderscore = false;
        }

        String sanitized = builder.length() == 0 ? "-" : builder.toString();
        return SENSITIVE_PATTERN.matcher(sanitized).replaceAll("$1=***");
    }
}
