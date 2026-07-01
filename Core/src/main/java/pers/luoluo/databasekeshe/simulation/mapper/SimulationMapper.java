package pers.luoluo.databasekeshe.simulation.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import pers.luoluo.databasekeshe.simulation.dto.SimulationDataRow;
import pers.luoluo.databasekeshe.simulation.dto.SimulationPointProfile;

@Mapper
public interface SimulationMapper {

    @Select("""
            SELECT
                TRANSFORMER_ID AS transformerId,
                CIRCUIT_ID AS circuitId,
                ID AS pointId,
                POINT_CODE AS pointCode,
                MEASURE_TYPE AS measureType,
                UNIT AS unit,
                MIN_LIMIT AS minLimit,
                MAX_LIMIT AS maxLimit
            FROM MEASURE_POINT
            WHERE STATUS = 0
            ORDER BY TRANSFORMER_ID, CIRCUIT_ID NULLS LAST, ID
            """)
    List<SimulationPointProfile> findPointProfiles();

    @Select("""
            SELECT *
            FROM (
                SELECT
                    DATA_ROW.*,
                    ROW_NUMBER() OVER (ORDER BY DATA_ROW.sampleTime DESC, DATA_ROW.id DESC) AS rowNumber
                FROM (
                    SELECT
                        R.ID AS id,
                        R.TRANSFORMER_ID AS transformerId,
                        BT.NAME AS transformerName,
                        R.CIRCUIT_ID AS circuitId,
                        PC.NAME AS circuitName,
                        R.POINT_ID AS pointId,
                        MP.POINT_NAME AS pointName,
                        MP.POINT_CODE AS pointCode,
                        MP.UNIT AS unit,
                        R.SAMPLE_TIME AS sampleTime,
                        R.VAL AS value,
                        R.QUALITY_FLAG AS qualityFlag,
                        R.CREATED_AT AS createdAt
                    FROM TS_RAW_DATA R
                    JOIN BOX_TRANSFORMER BT ON BT.ID = R.TRANSFORMER_ID
                    LEFT JOIN POWER_CIRCUIT PC ON PC.ID = R.CIRCUIT_ID
                    JOIN MEASURE_POINT MP ON MP.ID = R.POINT_ID
                    WHERE R.SAMPLE_TIME >= (
                        SELECT NVL(STARTED_AT, TIMESTAMP '1970-01-01 00:00:00')
                        FROM SIM_CONTROL
                        WHERE ID = 1
                    )
                ) DATA_ROW
            )
            WHERE rowNumber BETWEEN #{startRow} AND #{endRow}
            ORDER BY rowNumber
            """)
    List<SimulationDataRow> findRecentSimulationData(
            @Param("startRow") int startRow,
            @Param("endRow") int endRow
    );

    @Select("""
            SELECT COUNT(1)
            FROM TS_RAW_DATA R
            WHERE R.SAMPLE_TIME >= (
                SELECT NVL(STARTED_AT, TIMESTAMP '1970-01-01 00:00:00')
                FROM SIM_CONTROL
                WHERE ID = 1
            )
            """)
    long countRecentSimulationData();

    @Insert("""
            INSERT INTO TS_RAW_DATA (
                ID,
                TRANSFORMER_ID,
                CIRCUIT_ID,
                POINT_ID,
                SAMPLE_TIME,
                VAL,
                QUALITY_FLAG,
                CREATED_AT
            )
            VALUES (
                SEQ_TS_RAW_DATA.NEXTVAL,
                #{transformerId},
                #{circuitId},
                #{pointId},
                #{sampleTime},
                #{value},
                #{qualityFlag},
                LOCALTIMESTAMP
            )
            """)
    int insertRawData(
            @Param("transformerId") Long transformerId,
            @Param("circuitId") Long circuitId,
            @Param("pointId") Long pointId,
            @Param("sampleTime") LocalDateTime sampleTime,
            @Param("value") BigDecimal value,
            @Param("qualityFlag") int qualityFlag
    );

    @Select("SELECT SEQ_ALARM_LOG.NEXTVAL FROM DUAL")
    Long nextAlarmId();

    @Insert("""
            INSERT INTO ALARM_LOG (
                ID,
                TRANSFORMER_ID,
                CIRCUIT_ID,
                POINT_ID,
                ALARM_TYPE,
                ALARM_LEVEL,
                START_TIME,
                END_TIME,
                START_VAL,
                END_VAL,
                STATUS,
                CREATED_AT
            )
            VALUES (
                #{alarmId},
                #{transformerId},
                #{circuitId},
                #{pointId},
                #{alarmType},
                #{alarmLevel},
                #{startTime},
                NULL,
                #{startValue},
                #{endValue},
                0,
                LOCALTIMESTAMP
            )
            """)
    int insertAlarm(
            @Param("alarmId") Long alarmId,
            @Param("transformerId") Long transformerId,
            @Param("circuitId") Long circuitId,
            @Param("pointId") Long pointId,
            @Param("alarmType") String alarmType,
            @Param("alarmLevel") String alarmLevel,
            @Param("startTime") LocalDateTime startTime,
            @Param("startValue") BigDecimal startValue,
            @Param("endValue") BigDecimal endValue
    );

    @Insert("""
            INSERT INTO MAINT_TASK (
                TASK_ID,
                ALARM_ID,
                STATUS,
                ASSIGNEE,
                FEEDBACK,
                CREATED_AT,
                UPDATED_AT,
                FINISHED_AT
            )
            VALUES (
                SEQ_MAINT_TASK.NEXTVAL,
                #{alarmId},
                0,
                #{assignee},
                NULL,
                LOCALTIMESTAMP,
                NULL,
                NULL
            )
            """)
    int insertTask(@Param("alarmId") Long alarmId, @Param("assignee") String assignee);
}
