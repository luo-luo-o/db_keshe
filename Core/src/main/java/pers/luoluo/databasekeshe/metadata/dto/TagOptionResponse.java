package pers.luoluo.databasekeshe.metadata.dto;

import java.math.BigDecimal;

public record TagOptionResponse(
        Long id,
        String tagCode,
        String tagName,
        String unit,
        BigDecimal warnLimit,
        BigDecimal rateLimit
) {
}
