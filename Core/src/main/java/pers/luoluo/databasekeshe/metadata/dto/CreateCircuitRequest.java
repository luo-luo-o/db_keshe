package pers.luoluo.databasekeshe.metadata.dto;

import java.math.BigDecimal;

public record CreateCircuitRequest(
        String circuitCode,
        String circuitName,
        String direction,
        BigDecimal ratedVoltageKv,
        BigDecimal ratedCurrentA,
        Integer status
) {
}
