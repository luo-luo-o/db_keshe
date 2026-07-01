package pers.luoluo.databasekeshe.backup.dto;

import java.time.LocalDateTime;

public record BackupSnapshotResponse(
        Long id,
        String snapshotName,
        String note,
        String dumpFileName,
        String logFileName,
        String restoreLogFileName,
        String status,
        Long transformerCount,
        Long circuitCount,
        Long pointCount,
        Long rawDataCount,
        Long archiveCount,
        Long doorLogCount,
        Long alarmCount,
        Long taskCount,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime restoredAt,
        String restoredBy,
        String errorMessage
) {
}
