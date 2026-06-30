package pers.luoluo.databasekeshe.metadata.dto;

import java.math.BigDecimal;

public record UpdateMeasurePointRequest(
        Long circuitId,
        String pointCode,
        String pointName,
        String pointGroup,
        String measureType,
        String phaseCode,
        String unit,
        BigDecimal minLimit,
        BigDecimal maxLimit,
        BigDecimal rateLimit,
        Integer status
) {
}
