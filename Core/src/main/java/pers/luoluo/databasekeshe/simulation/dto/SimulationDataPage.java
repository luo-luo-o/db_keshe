package pers.luoluo.databasekeshe.simulation.dto;

import java.util.List;

public record SimulationDataPage(
        List<SimulationDataRow> rows,
        long total,
        int page,
        int size
) {
}
