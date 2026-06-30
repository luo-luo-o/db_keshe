package pers.luoluo.databasekeshe.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.common.time.DatabaseTimeOffsetService;
import pers.luoluo.databasekeshe.query.dto.HistoryDataRow;
import pers.luoluo.databasekeshe.query.dto.HistoryQueryRequest;
import pers.luoluo.databasekeshe.query.dto.MessageCategory;
import pers.luoluo.databasekeshe.query.dto.MessageQueryRequest;
import pers.luoluo.databasekeshe.query.dto.MessageResponse;
import pers.luoluo.databasekeshe.query.mapper.QueryMapper;
import pers.luoluo.databasekeshe.query.service.QueryService;
import pers.luoluo.databasekeshe.security.AccessGuard;
import pers.luoluo.databasekeshe.security.AuthenticatedUser;
import pers.luoluo.databasekeshe.security.RoleCode;

import static org.mockito.Mockito.mock;

class QueryServiceTests {

    private final QueryMapper queryMapper = mock(QueryMapper.class);
    private final AccessGuard accessGuard = mock(AccessGuard.class);
    private final DatabaseTimeOffsetService databaseTimeOffsetService =
            new DatabaseTimeOffsetService("Asia/Shanghai", "Asia/Shanghai");
    private final QueryService queryService =
            new QueryService(queryMapper, accessGuard, databaseTimeOffsetService);
    private final AuthenticatedUser adminUser = new AuthenticatedUser(1L, "admin", "Admin", RoleCode.ADMIN);

    @Test
    void queryHistoryKeepsApplicationWindowWhenDatabaseUsesShanghaiTimezone() {
        HistoryDataRow databaseRow = new HistoryDataRow(
                1L,
                1L,
                "T1",
                1L,
                "C1",
                1L,
                "P1",
                "CODE",
                "kV",
                LocalDateTime.of(2026, 6, 30, 8, 34, 30),
                LocalDateTime.of(2026, 6, 30, 8, 34, 30),
                java.math.BigDecimal.ONE,
                java.math.BigDecimal.ONE,
                java.math.BigDecimal.ONE,
                java.math.BigDecimal.ONE,
                1L,
                0,
                "RAW",
                LocalDateTime.of(2026, 6, 30, 8, 34, 31)
        );
        when(queryMapper.findHistory(
                eq(null),
                eq(null),
                eq(null),
                eq(LocalDateTime.of(2026, 6, 30, 11, 30, 0)),
                eq(LocalDateTime.of(2026, 6, 30, 18, 30, 0)),
                eq(50001)
        )).thenReturn(List.of(databaseRow));

        List<HistoryDataRow> rows = queryService.queryHistory(
                adminUser,
                new HistoryQueryRequest(
                        null,
                        null,
                        null,
                        LocalDateTime.of(2026, 6, 30, 11, 30, 0),
                        LocalDateTime.of(2026, 6, 30, 18, 30, 0)
                )
        );

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).sampleTime()).isEqualTo(LocalDateTime.of(2026, 6, 30, 8, 34, 30));
        verify(queryMapper).findHistory(
                null,
                null,
                null,
                LocalDateTime.of(2026, 6, 30, 11, 30, 0),
                LocalDateTime.of(2026, 6, 30, 18, 30, 0),
                50001
        );
    }

    @Test
    void queryMessagesUsesLatestShanghaiTimeWhenRollingWindowIsRequested() {
        when(queryMapper.findLatestSampleMessageTime(null, null, null, null))
                .thenReturn(LocalDateTime.of(2026, 6, 30, 8, 34, 30));
        when(queryMapper.findSampleMessages(
                eq(null),
                eq(null),
                eq(null),
                eq(LocalDateTime.of(2026, 6, 30, 7, 34, 30)),
                eq(LocalDateTime.of(2026, 6, 30, 8, 34, 30)),
                eq(null),
                eq(300)
        )).thenReturn(List.of(new MessageResponse(
                MessageCategory.SAMPLE,
                1L,
                1L,
                "T1",
                null,
                null,
                1L,
                "P1",
                "CODE",
                LocalDateTime.of(2026, 6, 30, 8, 34, 30),
                java.math.BigDecimal.ONE,
                "kV",
                0,
                null,
                null,
                null,
                null,
                null
        )));

        List<MessageResponse> rows = queryService.queryMessages(
                adminUser,
                new MessageQueryRequest(null, null, null, null, null, null, null)
        );

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).eventTime()).isEqualTo(LocalDateTime.of(2026, 6, 30, 8, 34, 30));
    }
}
