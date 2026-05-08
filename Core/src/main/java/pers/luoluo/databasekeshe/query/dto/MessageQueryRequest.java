package pers.luoluo.databasekeshe.query.dto;

import java.time.LocalDateTime;

public record MessageQueryRequest(
        MessageCategory category,
        Long deviceId,
        Long tagId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String keyword
) {
}
