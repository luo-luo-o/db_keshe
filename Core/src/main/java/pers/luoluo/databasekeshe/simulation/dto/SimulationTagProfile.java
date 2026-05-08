package pers.luoluo.databasekeshe.simulation.dto;

import java.math.BigDecimal;

public record SimulationTagProfile(
        Long deviceId,
        Long tagId,
        String tagCode,
        BigDecimal warnLimit,
        String unit
) {
}
