package pers.luoluo.databasekeshe.metadata.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTransformerRequest(
        String transformerCode,
        String transformerName,
        BigDecimal ratedCapacityKva,
        String ratedVoltageRatio,
        LocalDate commissionDate,
        String manufacturer,
        BigDecimal oilLevel,
        String location,
        Integer status
) {
}
