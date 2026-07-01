package pers.luoluo.databasekeshe.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pers.luoluo.databasekeshe.auth.dto.LoginRequest;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.auth.mapper.AuthMapper;
import pers.luoluo.databasekeshe.auth.service.AuthService;
import pers.luoluo.databasekeshe.auth.service.PasswordService;
import pers.luoluo.databasekeshe.common.exception.GlobalExceptionHandler;
import pers.luoluo.databasekeshe.logging.config.RuntimeLogInterceptor;
import pers.luoluo.databasekeshe.logging.config.WebMvcConfig;
import pers.luoluo.databasekeshe.logging.service.RuntimeLogService;
import pers.luoluo.databasekeshe.simulation.mapper.SimulationMapper;
import pers.luoluo.databasekeshe.simulation.service.SimulationService;

@ExtendWith(OutputCaptureExtension.class)
@WebMvcTest(controllers = TestLogController.class, properties = "app.log.dir=logs")
@Import({
        FileLoggingTests.TestConfig.class,
        RuntimeLogInterceptor.class,
        WebMvcConfig.class,
        GlobalExceptionHandler.class
})
class FileLoggingTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthMapper authMapper;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void resetMocks() {
        Mockito.reset(authMapper, jdbcTemplate);
    }

    @Test
    void successfulApiCallWritesAccessLogAndResponseRequestId() throws Exception {
        mockMvc.perform(get("/api/test/ping")
                        .header(RequestLogContext.USER_ID_HEADER, "42")
                        .header(RequestLogContext.ROLE_CODE_HEADER, "ADMIN")
                        .header(RequestLogContext.REQUEST_ID_HEADER, "req-access-1"))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestLogContext.REQUEST_ID_HEADER, "req-access-1"));

        String accessLog = waitForLogContaining(logDir().resolve("access.log"), "requestId=req-access-1");
        assertThat(accessLog).contains("requestId=req-access-1");
        assertThat(accessLog).contains("userId=42");
        assertThat(accessLog).contains("roleCode=ADMIN");
        assertThat(accessLog).contains("path=/api/test/ping");
        assertThat(accessLog).contains("status=200");
        assertThat(accessLog).contains("elapsedMs=");
    }

    @Test
    void exceptionsWriteApplicationLogWithoutPlaintextPassword() throws Exception {
        mockMvc.perform(get("/api/test/business-error")
                        .header(RequestLogContext.USER_ID_HEADER, "7")
                        .header(RequestLogContext.ROLE_CODE_HEADER, "ENGINEER")
                        .header(RequestLogContext.REQUEST_ID_HEADER, "req-warn-1"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/test/runtime-error")
                        .header(RequestLogContext.USER_ID_HEADER, "7")
                        .header(RequestLogContext.ROLE_CODE_HEADER, "ENGINEER")
                        .header(RequestLogContext.REQUEST_ID_HEADER, "req-error-1"))
                .andExpect(status().isInternalServerError());

        String applicationLog = waitForLogContaining(logDir().resolve("application.log"), "requestId=req-warn-1");
        assertThat(applicationLog).contains("requestId=req-warn-1");
        assertThat(applicationLog).contains("业务异常 status=409");
        String runtimeLog = waitForLogContaining(logDir().resolve("application.log"), "requestId=req-error-1");
        assertThat(runtimeLog).contains("requestId=req-error-1");
        assertThat(runtimeLog).contains("message=database password=***");
        assertThat(runtimeLog).doesNotContain("password=secret123");
        assertThat(applicationLog).doesNotContain("secret123");
    }

    @Test
    void simulationWriteTickFailureWritesApplicationLog(CapturedOutput output) throws Exception {
        doThrow(new DataAccessResourceFailureException("tick password=secret456 failed"))
                .when(jdbcTemplate)
                .execute(any(CallableStatementCreator.class), org.mockito.ArgumentMatchers.<CallableStatementCallback<Object>>any());

        SimulationService simulationService = new SimulationService(
                jdbcTemplate,
                new RuntimeLogService(),
                Mockito.mock(SimulationMapper.class)
        );
        simulationService.writeTick();

        String applicationLog = waitForLogContaining(logDir().resolve("application.log"), "Database simulation tick failed");
        assertThat(applicationLog).contains("Database simulation tick failed");
        assertThat(applicationLog).contains("password=***");
        assertThat(applicationLog).doesNotContain("secret456");
        assertThat(output.getOut()).doesNotContain("secret456");
    }

    @Test
    void authFailuresWriteSanitizedApplicationLog() throws Exception {
        when(authMapper.findByUsername("alice")).thenReturn(null);

        AuthService authService = new AuthService(authMapper, new PasswordService());
        try {
            authService.login(new LoginRequest("alice", "super-secret"));
        } catch (AuthException ignored) {
        }

        String applicationLog = waitForLogContaining(logDir().resolve("application.log"), "event=auth_login username=alice");
        assertThat(applicationLog).contains("event=auth_login");
        assertThat(applicationLog).contains("username=alice");
        assertThat(applicationLog).doesNotContain("super-secret");
    }

    private Path logDir() {
        return Path.of("logs");
    }

    private String waitForLogContaining(Path path, String expectedText) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(5).toNanos();
        while (System.nanoTime() < deadline) {
            if (Files.exists(path)) {
                String text = Files.readString(path, StandardCharsets.UTF_8);
                if (text.contains(expectedText)) {
                    return text;
                }
            }
            Thread.sleep(100L);
        }
        throw new AssertionError("Log file did not contain expected text: " + expectedText + " in " + path);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        PasswordService passwordService() {
            return new PasswordService();
        }

        @Bean
        RuntimeLogService runtimeLogService() {
            return new RuntimeLogService();
        }

    }
}
