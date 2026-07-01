package pers.luoluo.databasekeshe.backup.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.backup.dto.BackupSnapshotResponse;
import pers.luoluo.databasekeshe.backup.dto.CreateBackupRequest;
import pers.luoluo.databasekeshe.backup.mapper.BackupMapper;
import pers.luoluo.databasekeshe.security.AccessGuard;
import pers.luoluo.databasekeshe.security.AuthenticatedUser;
import pers.luoluo.databasekeshe.security.RoleCode;

@Service
public class BackupService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackupService.class);

    private final BackupMapper backupMapper;
    private final AccessGuard accessGuard;
    private final JdbcTemplate jdbcTemplate;

    public BackupService(BackupMapper backupMapper, AccessGuard accessGuard, JdbcTemplate jdbcTemplate) {
        this.backupMapper = backupMapper;
        this.accessGuard = accessGuard;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BackupSnapshotResponse> snapshots(AuthenticatedUser user) {
        requireAdmin(user);
        return backupMapper.findSnapshots();
    }

    public BackupSnapshotResponse createSnapshot(AuthenticatedUser user, CreateBackupRequest request) {
        requireAdmin(user);
        String snapshotName = normalize(request == null ? null : request.snapshotName());
        String note = normalize(request == null ? null : request.note());
        Long snapshotId;
        try {
            snapshotId = jdbcTemplate.execute((CallableStatementCreator) (Connection connection) -> {
                var statement = connection.prepareCall("{call PKG_PSM_BACKUP.CREATE_BACKUP(?, ?, ?, ?)}");
                statement.setString(1, snapshotName);
                statement.setString(2, note);
                statement.setString(3, user.username());
                statement.registerOutParameter(4, Types.NUMERIC);
                return statement;
            }, (CallableStatementCallback<Long>) statement -> {
                statement.execute();
                return statement.getLong(4);
            });
        } catch (DataAccessException exception) {
            LOGGER.warn("event=data_backup_create userId={} username={} result=FAILED reason={}",
                    user.userId(), sanitize(user.username()), sanitize(rootSqlMessage(exception)));
            throw new AuthException(HttpStatus.BAD_REQUEST, "创建备份失败：" + backupFailureReason(exception));
        }

        BackupSnapshotResponse snapshot = backupMapper.findSnapshotById(snapshotId);
        if (snapshot == null) {
            throw new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, "创建备份失败");
        }
        LOGGER.info("event=data_backup_create userId={} username={} snapshotId={} result=SUCCESS",
                user.userId(), sanitize(user.username()), snapshotId);
        return snapshot;
    }

    public BackupSnapshotResponse restoreSnapshot(AuthenticatedUser user, Long snapshotId) {
        requireAdmin(user);
        if (snapshotId == null || backupMapper.findSnapshotById(snapshotId) == null) {
            throw new AuthException(HttpStatus.NOT_FOUND, "备份不存在");
        }

        try {
            jdbcTemplate.execute((CallableStatementCreator) (Connection connection) -> {
                var statement = connection.prepareCall("{call PKG_PSM_BACKUP.RESTORE_BACKUP(?, ?)}");
                statement.setLong(1, snapshotId);
                statement.setString(2, user.username());
                return statement;
            }, (CallableStatementCallback<Void>) statement -> {
                statement.execute();
                return null;
            });
        } catch (DataAccessException exception) {
            LOGGER.warn("event=data_backup_restore userId={} username={} snapshotId={} result=FAILED reason={}",
                    user.userId(), sanitize(user.username()), snapshotId, sanitize(rootSqlMessage(exception)));
            throw new AuthException(HttpStatus.BAD_REQUEST, "回溯备份失败：" + backupFailureReason(exception));
        }

        BackupSnapshotResponse snapshot = backupMapper.findSnapshotById(snapshotId);
        LOGGER.info("event=data_backup_restore userId={} username={} snapshotId={} result=SUCCESS",
                user.userId(), sanitize(user.username()), snapshotId);
        return snapshot;
    }

    private void requireAdmin(AuthenticatedUser user) {
        accessGuard.requireAny(user, RoleCode.ADMIN);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String sanitize(String value) {
        return value == null ? "-" : value.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private String backupFailureReason(DataAccessException exception) {
        String message = rootSqlMessage(exception);
        if (message == null || message.isBlank()) {
            return "请检查 Data Pump 目录授权和数据库运行日志";
        }

        if (message.contains("ORA-39087") || message.contains("ORA-29283") || message.contains("PSM_BACKUP_DIR")) {
            return "Data Pump 目录 PSM_BACKUP_DIR 不可用，请先执行 oracle21c-admin-datapump.sql 并确认目录可写";
        }

        return "请检查备份元数据、Data Pump 日志和数据库运行日志";
    }

    private String rootSqlMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SQLException) {
                return current.getMessage();
            }
            current = current.getCause();
        }
        return throwable == null ? null : throwable.getMessage();
    }
}
