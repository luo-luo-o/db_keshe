package pers.luoluo.databasekeshe.backup.dto;

import java.time.LocalDateTime;

public record BackupSnapshotResponse(
        Long id,
        String snapshotName,
        String note,
        Long transformerCount,
        Long circuitCount,
        Long pointCount,
        Long rawDataCount,
        Long alarmCount,
        Long taskCount,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime restoredAt,
        String restoredBy
) {
}
