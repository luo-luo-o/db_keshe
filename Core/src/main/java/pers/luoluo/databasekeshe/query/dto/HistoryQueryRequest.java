package pers.luoluo.databasekeshe.query.dto;

import java.time.LocalDateTime;

public record HistoryQueryRequest(
        Long deviceId,
        Long tagId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        Integer freqFlag
) {
}
