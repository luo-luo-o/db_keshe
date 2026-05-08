package pers.luoluo.databasekeshe.simulation.mapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import pers.luoluo.databasekeshe.simulation.dto.SimulationTagProfile;

@Mapper
public interface SimulationMapper {

    @Select("""
            SELECT
                d.ID AS deviceId,
                t.ID AS tagId,
                t.TAG_CODE AS tagCode,
                t.WARN_LIMIT AS warnLimit,
                t.UNIT AS unit
            FROM TAG_BASE t
            JOIN DEVICE_BASE d ON d.ID = t.DEVICE_ID
            ORDER BY d.ID, t.ID
            """)
    List<SimulationTagProfile> findTagProfiles();

    @Insert("""
            INSERT INTO TS_RAW_DATA (
                ID,
                DEVICE_ID,
                TAG_ID,
                SAMPLE_TIME,
                VAL,
                FREQ_FLAG,
                QUALITY_FLAG,
                CREATED_AT
            )
            VALUES (
                SEQ_TS_RAW_DATA.NEXTVAL,
                #{deviceId},
                #{tagId},
                #{sampleTime},
                #{value},
                #{freqFlag},
                #{qualityFlag},
                SYSTIMESTAMP
            )
            """)
    int insertRawData(
            @Param("deviceId") Long deviceId,
            @Param("tagId") Long tagId,
            @Param("sampleTime") LocalDateTime sampleTime,
            @Param("value") BigDecimal value,
            @Param("freqFlag") int freqFlag,
            @Param("qualityFlag") int qualityFlag
    );

    @Select("SELECT SEQ_ALARM_LOG.NEXTVAL FROM DUAL")
    Long nextAlarmId();

    @Insert("""
            INSERT INTO ALARM_LOG (
                ID,
                DEVICE_ID,
                TAG_ID,
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
                #{deviceId},
                #{tagId},
                #{alarmType},
                #{alarmLevel},
                #{startTime},
                NULL,
                #{startValue},
                #{endValue},
                0,
                SYSTIMESTAMP
            )
            """)
    int insertAlarm(
            @Param("alarmId") Long alarmId,
            @Param("deviceId") Long deviceId,
            @Param("tagId") Long tagId,
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
                SYSTIMESTAMP,
                NULL,
                NULL
            )
            """)
    int insertTask(@Param("alarmId") Long alarmId, @Param("assignee") String assignee);
}
