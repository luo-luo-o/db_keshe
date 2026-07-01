package pers.luoluo.databasekeshe.backup.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import pers.luoluo.databasekeshe.backup.dto.BackupSnapshotResponse;

@Mapper
public interface BackupMapper {

    @Select("""
            SELECT
                ID AS id,
                SNAPSHOT_NAME AS snapshotName,
                NOTE AS note,
                DUMP_FILE_NAME AS dumpFileName,
                LOG_FILE_NAME AS logFileName,
                RESTORE_LOG_FILE_NAME AS restoreLogFileName,
                STATUS AS status,
                TRANSFORMER_COUNT AS transformerCount,
                CIRCUIT_COUNT AS circuitCount,
                POINT_COUNT AS pointCount,
                RAW_DATA_COUNT AS rawDataCount,
                ARCHIVE_COUNT AS archiveCount,
                DOOR_LOG_COUNT AS doorLogCount,
                ALARM_COUNT AS alarmCount,
                TASK_COUNT AS taskCount,
                CREATED_BY AS createdBy,
                CREATED_AT AS createdAt,
                RESTORED_AT AS restoredAt,
                RESTORED_BY AS restoredBy,
                ERROR_MESSAGE AS errorMessage
            FROM DATA_SNAPSHOT
            ORDER BY CREATED_AT DESC, ID DESC
            """)
    List<BackupSnapshotResponse> findSnapshots();

    @Select("""
            SELECT
                ID AS id,
                SNAPSHOT_NAME AS snapshotName,
                NOTE AS note,
                DUMP_FILE_NAME AS dumpFileName,
                LOG_FILE_NAME AS logFileName,
                RESTORE_LOG_FILE_NAME AS restoreLogFileName,
                STATUS AS status,
                TRANSFORMER_COUNT AS transformerCount,
                CIRCUIT_COUNT AS circuitCount,
                POINT_COUNT AS pointCount,
                RAW_DATA_COUNT AS rawDataCount,
                ARCHIVE_COUNT AS archiveCount,
                DOOR_LOG_COUNT AS doorLogCount,
                ALARM_COUNT AS alarmCount,
                TASK_COUNT AS taskCount,
                CREATED_BY AS createdBy,
                CREATED_AT AS createdAt,
                RESTORED_AT AS restoredAt,
                RESTORED_BY AS restoredBy,
                ERROR_MESSAGE AS errorMessage
            FROM DATA_SNAPSHOT
            WHERE ID = #{snapshotId}
            """)
    BackupSnapshotResponse findSnapshotById(Long snapshotId);
}
