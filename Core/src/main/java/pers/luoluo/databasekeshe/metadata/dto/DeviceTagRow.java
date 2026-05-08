package pers.luoluo.databasekeshe.metadata.dto;

import java.math.BigDecimal;

public record DeviceTagRow(
        Long stationId,
        String stationName,
        Long bayId,
        String bayName,
        Long deviceId,
        String deviceName,
        String deviceType,
        Integer status,
        BigDecimal currentLimit,
        BigDecimal tempLimit,
        BigDecimal tempRateLimit,
        Long tagId,
        String tagCode,
        String tagName,
        String unit,
        BigDecimal warnLimit,
        BigDecimal rateLimit
) {
}
