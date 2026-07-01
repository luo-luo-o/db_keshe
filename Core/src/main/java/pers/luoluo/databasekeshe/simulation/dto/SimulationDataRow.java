package pers.luoluo.databasekeshe.simulation.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SimulationDataRow(
        Long id,
        Long transformerId,
        String transformerName,
        Long circuitId,
        String circuitName,
        Long pointId,
        String pointName,
        String pointCode,
        String unit,
        LocalDateTime sampleTime,
        BigDecimal value,
        Integer qualityFlag,
        LocalDateTime createdAt
) {
}
