package pers.luoluo.databasekeshe.logging;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.luoluo.databasekeshe.auth.exception.AuthException;

@RestController
@RequestMapping("/api/test")
class TestLogController {

    @GetMapping("/ping")
    String ping() {
        return "ok";
    }

    @GetMapping("/business-error")
    String businessError() {
        throw new AuthException(HttpStatus.CONFLICT, "业务失败");
    }

    @GetMapping("/runtime-error")
    String runtimeError() {
        throw new IllegalStateException("database password=secret123");
    }
}
