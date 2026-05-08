package pers.luoluo.databasekeshe.query.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HistoryDataRow(
        Long id,
        Long deviceId,
        String deviceName,
        Long tagId,
        String tagName,
        String tagCode,
        String unit,
        LocalDateTime sampleTime,
        BigDecimal value,
        Integer freqFlag,
        Integer qualityFlag,
        LocalDateTime createdAt
) {
}
