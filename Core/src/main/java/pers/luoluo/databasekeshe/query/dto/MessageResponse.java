package pers.luoluo.databasekeshe.query.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MessageResponse(
        MessageCategory category,
        Long id,
        Long deviceId,
        String deviceName,
        Long tagId,
        String tagName,
        String tagCode,
        LocalDateTime eventTime,
        BigDecimal value,
        String unit,
        Integer freqFlag,
        Integer qualityFlag,
        String alarmType,
        String alarmLevel,
        Integer status,
        String assignee,
        String feedback
) {
}
