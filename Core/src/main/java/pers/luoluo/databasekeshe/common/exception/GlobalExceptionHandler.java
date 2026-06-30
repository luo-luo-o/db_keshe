package pers.luoluo.databasekeshe.common.exception;

import java.sql.SQLRecoverableException;
import java.sql.SQLTransientConnectionException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.common.dto.ApiErrorResponse;
import pers.luoluo.databasekeshe.logging.service.RuntimeLogService;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private final RuntimeLogService runtimeLogService;

    public GlobalExceptionHandler(RuntimeLogService runtimeLogService) {
        this.runtimeLogService = runtimeLogService;
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiErrorResponse> handleAuthException(AuthException exception) {
        runtimeLogService.warn(exception.getMessage(), "业务异常 status=" + exception.status().value());
        return ResponseEntity.status(exception.status()).body(new ApiErrorResponse(exception.getMessage()));
    }

    @ExceptionHandler({
            CannotGetJdbcConnectionException.class,
            SQLTransientConnectionException.class,
            SQLRecoverableException.class
    })
    public ResponseEntity<ApiErrorResponse> handleConnectionException(Exception exception) {
        runtimeLogService.error(exception.getMessage(), "database connection " + exception.getClass().getName(), exception);
        return ResponseEntity.status(503).body(new ApiErrorResponse("服务端连接失败，请稍后重试"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleException(Exception exception) {
        runtimeLogService.error(exception.getMessage(), exception.getClass().getName(), exception);
        return ResponseEntity.internalServerError().body(new ApiErrorResponse("服务端处理失败"));
    }
}
