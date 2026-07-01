package pers.luoluo.databasekeshe.query.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.common.time.DatabaseTimeOffsetService;
import pers.luoluo.databasekeshe.query.dto.HistoryDataRow;
import pers.luoluo.databasekeshe.query.dto.HistoryQueryRequest;
import pers.luoluo.databasekeshe.query.dto.MessageCategory;
import pers.luoluo.databasekeshe.query.dto.MessageQueryRequest;
import pers.luoluo.databasekeshe.query.dto.MessageResponse;
import pers.luoluo.databasekeshe.query.mapper.QueryMapper;
import pers.luoluo.databasekeshe.security.AccessGuard;
import pers.luoluo.databasekeshe.security.AuthenticatedUser;
import pers.luoluo.databasekeshe.security.RoleCode;

@Service
public class QueryService {

    private static final int MESSAGE_LIMIT = 300;
    private static final int HISTORY_LIMIT = 50000;
    private static final int HISTORY_FETCH_LIMIT = HISTORY_LIMIT + 1;

    private final QueryMapper queryMapper;
    private final AccessGuard accessGuard;
    private final DatabaseTimeOffsetService databaseTimeOffsetService;

    public QueryService(QueryMapper queryMapper, AccessGuard accessGuard, DatabaseTimeOffsetService databaseTimeOffsetService) {
        this.queryMapper = queryMapper;
        this.accessGuard = accessGuard;
        this.databaseTimeOffsetService = databaseTimeOffsetService;
    }

    public List<MessageResponse> queryMessages(AuthenticatedUser user, MessageQueryRequest request) {
        List<MessageCategory> categories = resolveCategories(user, request.category());
        LocalDateTime applicationEndTime = request.endTime();
        LocalDateTime applicationStartTime = request.startTime();
        String keyword = normalizedKeyword(request.keyword());

        if (applicationStartTime == null || applicationEndTime == null) {
            LocalDateTime latestEventTime = resolveLatestMessageTime(categories, request, keyword);
            if (latestEventTime != null) {
                applicationEndTime = databaseTimeOffsetService.toApplicationTime(latestEventTime);
            }
        }

        LocalDateTime endTime = databaseTimeOffsetService.toDatabaseTime(
                applicationEndTime == null ? databaseTimeOffsetService.applicationNow() : applicationEndTime
        );
        LocalDateTime startTime = databaseTimeOffsetService.toDatabaseTime(
                applicationStartTime == null
                        ? databaseTimeOffsetService.toApplicationTime(endTime).minusHours(1)
                        : applicationStartTime
        );
        validateTimeRange(startTime, endTime);

        List<MessageResponse> responses = new ArrayList<>();
        for (MessageCategory category : categories) {
            responses.addAll(switch (category) {
                case SAMPLE -> queryMapper.findSampleMessages(
                        request.transformerId(),
                        request.circuitId(),
                        request.pointId(),
                        startTime,
                        endTime,
                        keyword,
                        MESSAGE_LIMIT
                );
                case ALARM -> queryMapper.findAlarmMessages(
                        request.transformerId(),
                        request.circuitId(),
                        request.pointId(),
                        startTime,
                        endTime,
                        keyword,
                        MESSAGE_LIMIT
                );
                case TASK -> queryMapper.findTaskMessages(
                        request.transformerId(),
                        request.circuitId(),
                        request.pointId(),
                        startTime,
                        endTime,
                        keyword,
                        MESSAGE_LIMIT
                );
            });
        }

        return responses.stream()
                .map(this::toApplicationMessage)
                .sorted(Comparator.comparing(MessageResponse::eventTime).reversed())
                .limit(MESSAGE_LIMIT)
                .toList();
    }

    public List<HistoryDataRow> queryHistory(AuthenticatedUser user, HistoryQueryRequest request) {
        accessGuard.requireAny(user, RoleCode.ADMIN, RoleCode.ENGINEER, RoleCode.MANAGER);

        LocalDateTime applicationEndTime = request.endTime();
        if (applicationEndTime == null) {
            LocalDateTime latestHistoryTime = queryMapper.findLatestHistoryTime(
                    request.transformerId(),
                    request.circuitId(),
                    request.pointId()
            );
            if (latestHistoryTime != null) {
                applicationEndTime = databaseTimeOffsetService.toApplicationTime(latestHistoryTime);
            }
        }

        LocalDateTime endTime = databaseTimeOffsetService.toDatabaseTime(
                applicationEndTime == null ? databaseTimeOffsetService.applicationNow() : applicationEndTime
        );
        LocalDateTime startTime = databaseTimeOffsetService.toDatabaseTime(
                request.startTime() == null
                        ? databaseTimeOffsetService.toApplicationTime(endTime).minusHours(1)
                        : request.startTime()
        );
        validateTimeRange(startTime, endTime);

        List<HistoryDataRow> rows = queryMapper.findHistory(
                request.transformerId(),
                request.circuitId(),
                request.pointId(),
                startTime,
                endTime,
                HISTORY_FETCH_LIMIT
        );

        if (rows.size() > HISTORY_LIMIT) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "History range is too large. Narrow the time window before querying.");
        }

        return rows.stream().map(this::toApplicationHistory).toList();
    }

    private List<MessageCategory> resolveCategories(AuthenticatedUser user, MessageCategory requestedCategory) {
        List<MessageCategory> allowedCategories = switch (user.roleCode()) {
            case ADMIN -> List.of(MessageCategory.SAMPLE, MessageCategory.ALARM, MessageCategory.TASK);
            case ENGINEER -> List.of(MessageCategory.ALARM, MessageCategory.TASK);
            case MANAGER -> List.of(MessageCategory.SAMPLE, MessageCategory.ALARM, MessageCategory.TASK);
        };

        if (requestedCategory == null) {
            return allowedCategories;
        }

        if (!allowedCategories.contains(requestedCategory)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Current role cannot query this message category.");
        }

        return List.of(requestedCategory);
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Start time cannot be after end time.");
        }
    }

    private LocalDateTime resolveLatestMessageTime(
            List<MessageCategory> categories,
            MessageQueryRequest request,
            String keyword
    ) {
        return categories.stream()
                .map(category -> switch (category) {
                    case SAMPLE -> queryMapper.findLatestSampleMessageTime(
                            request.transformerId(),
                            request.circuitId(),
                            request.pointId(),
                            keyword
                    );
                    case ALARM -> queryMapper.findLatestAlarmMessageTime(
                            request.transformerId(),
                            request.circuitId(),
                            request.pointId(),
                            keyword
                    );
                    case TASK -> queryMapper.findLatestTaskMessageTime(
                            request.transformerId(),
                            request.circuitId(),
                            request.pointId(),
                            keyword
                    );
                })
                .filter(java.util.Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }

    private MessageResponse toApplicationMessage(MessageResponse row) {
        return new MessageResponse(
                row.category(),
                row.id(),
                row.transformerId(),
                row.transformerName(),
                row.circuitId(),
                row.circuitName(),
                row.pointId(),
                row.pointName(),
                row.pointCode(),
                databaseTimeOffsetService.toApplicationTime(row.eventTime()),
                row.value(),
                row.unit(),
                row.qualityFlag(),
                row.alarmType(),
                row.alarmLevel(),
                row.status(),
                row.assignee(),
                row.feedback()
        );
    }

    private HistoryDataRow toApplicationHistory(HistoryDataRow row) {
        return new HistoryDataRow(
                row.id(),
                row.transformerId(),
                row.transformerName(),
                row.circuitId(),
                row.circuitName(),
                row.pointId(),
                row.pointName(),
                row.pointCode(),
                row.unit(),
                databaseTimeOffsetService.toApplicationTime(row.sampleTime()),
                databaseTimeOffsetService.toApplicationTime(row.rangeEndTime()),
                row.value(),
                row.avgValue(),
                row.minValue(),
                row.maxValue(),
                row.sampleCount(),
                row.qualityFlag(),
                row.granularity(),
                databaseTimeOffsetService.toApplicationTime(row.createdAt())
        );
    }

    private String normalizedKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
