package pers.luoluo.databasekeshe.backup.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.luoluo.databasekeshe.backup.dto.BackupSnapshotResponse;
import pers.luoluo.databasekeshe.backup.dto.CreateBackupRequest;
import pers.luoluo.databasekeshe.backup.service.BackupService;
import pers.luoluo.databasekeshe.security.AccessGuard;
import pers.luoluo.databasekeshe.security.AuthenticatedUser;

@RestController
@RequestMapping("/api/backups")
public class BackupController {

    private final BackupService backupService;
    private final AccessGuard accessGuard;

    public BackupController(BackupService backupService, AccessGuard accessGuard) {
        this.backupService = backupService;
        this.accessGuard = accessGuard;
    }

    @GetMapping
    public List<BackupSnapshotResponse> snapshots(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode
    ) {
        AuthenticatedUser user = accessGuard.requireUser(userId, roleCode);
        return backupService.snapshots(user);
    }

    @PostMapping
    public BackupSnapshotResponse createSnapshot(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @RequestBody(required = false) CreateBackupRequest request
    ) {
        AuthenticatedUser user = accessGuard.requireUser(userId, roleCode);
        return backupService.createSnapshot(user, request);
    }

    @PostMapping("/{snapshotId}/restore")
    public BackupSnapshotResponse restoreSnapshot(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long snapshotId
    ) {
        AuthenticatedUser user = accessGuard.requireUser(userId, roleCode);
        return backupService.restoreSnapshot(user, snapshotId);
    }
}
