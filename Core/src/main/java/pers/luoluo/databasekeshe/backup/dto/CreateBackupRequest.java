package pers.luoluo.databasekeshe.backup.dto;

public record CreateBackupRequest(
        String snapshotName,
        String note
) {
}
