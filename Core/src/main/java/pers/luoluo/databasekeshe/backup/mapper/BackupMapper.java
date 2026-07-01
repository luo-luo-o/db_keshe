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
                TRANSFORMER_COUNT AS transformerCount,
                CIRCUIT_COUNT AS circuitCount,
                POINT_COUNT AS pointCount,
                RAW_DATA_COUNT AS rawDataCount,
                ALARM_COUNT AS alarmCount,
                TASK_COUNT AS taskCount,
                CREATED_BY AS createdBy,
                CREATED_AT AS createdAt,
                RESTORED_AT AS restoredAt,
                RESTORED_BY AS restoredBy
            FROM DATA_SNAPSHOT
            ORDER BY CREATED_AT DESC, ID DESC
            """)
    List<BackupSnapshotResponse> findSnapshots();

    @Select("""
            SELECT
                ID AS id,
                SNAPSHOT_NAME AS snapshotName,
                NOTE AS note,
                TRANSFORMER_COUNT AS transformerCount,
                CIRCUIT_COUNT AS circuitCount,
                POINT_COUNT AS pointCount,
                RAW_DATA_COUNT AS rawDataCount,
                ALARM_COUNT AS alarmCount,
                TASK_COUNT AS taskCount,
                CREATED_BY AS createdBy,
                CREATED_AT AS createdAt,
                RESTORED_AT AS restoredAt,
                RESTORED_BY AS restoredBy
            FROM DATA_SNAPSHOT
            WHERE ID = #{snapshotId}
            """)
    BackupSnapshotResponse findSnapshotById(Long snapshotId);
}
