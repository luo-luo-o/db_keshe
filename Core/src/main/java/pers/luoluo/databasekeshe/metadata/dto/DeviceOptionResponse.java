package pers.luoluo.databasekeshe.metadata.dto;

import java.math.BigDecimal;
import java.util.List;

public record DeviceOptionResponse(
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
        List<TagOptionResponse> tags
) {
}
