package pers.luoluo.databasekeshe.query.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import pers.luoluo.databasekeshe.query.dto.HistoryDataRow;
import pers.luoluo.databasekeshe.query.dto.MessageResponse;

@Mapper
public interface QueryMapper {

    @Select("""
            <script>
            SELECT *
            FROM (
                SELECT
                    'SAMPLE' AS category,
                    r.ID AS id,
                    r.DEVICE_ID AS deviceId,
                    d.NAME AS deviceName,
                    r.TAG_ID AS tagId,
                    t.TAG_NAME AS tagName,
                    t.TAG_CODE AS tagCode,
                    r.SAMPLE_TIME AS eventTime,
                    r.VAL AS value,
                    t.UNIT AS unit,
                    r.FREQ_FLAG AS freqFlag,
                    r.QUALITY_FLAG AS qualityFlag,
                    NULL AS alarmType,
                    NULL AS alarmLevel,
                    NULL AS status,
                    NULL AS assignee,
                    NULL AS feedback
                FROM TS_RAW_DATA r
                JOIN DEVICE_BASE d ON d.ID = r.DEVICE_ID
                LEFT JOIN TAG_BASE t ON t.ID = r.TAG_ID
                WHERE r.SAMPLE_TIME BETWEEN #{startTime} AND #{endTime}
                <if test="deviceId != null">
                    AND r.DEVICE_ID = #{deviceId}
                </if>
                <if test="tagId != null">
                    AND r.TAG_ID = #{tagId}
                </if>
                <if test="keyword != null and keyword != ''">
                    AND (
                        LOWER(d.NAME) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(t.TAG_NAME) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(t.TAG_CODE) LIKE '%' || LOWER(#{keyword}) || '%'
                    )
                </if>
                ORDER BY r.SAMPLE_TIME DESC, r.ID DESC
            )
            WHERE ROWNUM &lt;= #{limit}
            </script>
            """)
    List<MessageResponse> findSampleMessages(
            @Param("deviceId") Long deviceId,
            @Param("tagId") Long tagId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Select("""
            <script>
            SELECT *
            FROM (
                SELECT
                    'ALARM' AS category,
                    a.ID AS id,
                    a.DEVICE_ID AS deviceId,
                    d.NAME AS deviceName,
                    a.TAG_ID AS tagId,
                    t.TAG_NAME AS tagName,
                    t.TAG_CODE AS tagCode,
                    a.START_TIME AS eventTime,
                    a.START_VAL AS value,
                    t.UNIT AS unit,
                    NULL AS freqFlag,
                    NULL AS qualityFlag,
                    a.ALARM_TYPE AS alarmType,
                    a.ALARM_LEVEL AS alarmLevel,
                    a.STATUS AS status,
                    NULL AS assignee,
                    NULL AS feedback
                FROM ALARM_LOG a
                JOIN DEVICE_BASE d ON d.ID = a.DEVICE_ID
                LEFT JOIN TAG_BASE t ON t.ID = a.TAG_ID
                WHERE a.START_TIME BETWEEN #{startTime} AND #{endTime}
                <if test="deviceId != null">
                    AND a.DEVICE_ID = #{deviceId}
                </if>
                <if test="tagId != null">
                    AND a.TAG_ID = #{tagId}
                </if>
                <if test="keyword != null and keyword != ''">
                    AND (
                        LOWER(d.NAME) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(t.TAG_NAME) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(t.TAG_CODE) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(a.ALARM_TYPE) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(a.ALARM_LEVEL) LIKE '%' || LOWER(#{keyword}) || '%'
                    )
                </if>
                ORDER BY a.START_TIME DESC, a.ID DESC
            )
            WHERE ROWNUM &lt;= #{limit}
            </script>
            """)
    List<MessageResponse> findAlarmMessages(
            @Param("deviceId") Long deviceId,
            @Param("tagId") Long tagId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Select("""
            <script>
            SELECT *
            FROM (
                SELECT
                    'TASK' AS category,
                    mt.TASK_ID AS id,
                    a.DEVICE_ID AS deviceId,
                    d.NAME AS deviceName,
                    a.TAG_ID AS tagId,
                    tb.TAG_NAME AS tagName,
                    tb.TAG_CODE AS tagCode,
                    mt.CREATED_AT AS eventTime,
                    a.START_VAL AS value,
                    tb.UNIT AS unit,
                    NULL AS freqFlag,
                    NULL AS qualityFlag,
                    a.ALARM_TYPE AS alarmType,
                    a.ALARM_LEVEL AS alarmLevel,
                    mt.STATUS AS status,
                    mt.ASSIGNEE AS assignee,
                    mt.FEEDBACK AS feedback
                FROM MAINT_TASK mt
                JOIN ALARM_LOG a ON a.ID = mt.ALARM_ID
                JOIN DEVICE_BASE d ON d.ID = a.DEVICE_ID
                LEFT JOIN TAG_BASE tb ON tb.ID = a.TAG_ID
                WHERE mt.CREATED_AT BETWEEN #{startTime} AND #{endTime}
                <if test="deviceId != null">
                    AND a.DEVICE_ID = #{deviceId}
                </if>
                <if test="tagId != null">
                    AND a.TAG_ID = #{tagId}
                </if>
                <if test="keyword != null and keyword != ''">
                    AND (
                        LOWER(d.NAME) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(tb.TAG_NAME) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(tb.TAG_CODE) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(mt.ASSIGNEE) LIKE '%' || LOWER(#{keyword}) || '%'
                        OR LOWER(mt.FEEDBACK) LIKE '%' || LOWER(#{keyword}) || '%'
                    )
                </if>
                ORDER BY mt.CREATED_AT DESC, mt.TASK_ID DESC
            )
            WHERE ROWNUM &lt;= #{limit}
            </script>
            """)
    List<MessageResponse> findTaskMessages(
            @Param("deviceId") Long deviceId,
            @Param("tagId") Long tagId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );

    @Select("""
            <script>
            SELECT *
            FROM (
                SELECT
                    r.ID AS id,
                    r.DEVICE_ID AS deviceId,
                    d.NAME AS deviceName,
                    r.TAG_ID AS tagId,
                    t.TAG_NAME AS tagName,
                    t.TAG_CODE AS tagCode,
                    t.UNIT AS unit,
                    r.SAMPLE_TIME AS sampleTime,
                    r.VAL AS value,
                    r.FREQ_FLAG AS freqFlag,
                    r.QUALITY_FLAG AS qualityFlag,
                    r.CREATED_AT AS createdAt
                FROM TS_RAW_DATA r
                JOIN DEVICE_BASE d ON d.ID = r.DEVICE_ID
                LEFT JOIN TAG_BASE t ON t.ID = r.TAG_ID
                WHERE r.SAMPLE_TIME BETWEEN #{startTime} AND #{endTime}
                <if test="deviceId != null">
                    AND r.DEVICE_ID = #{deviceId}
                </if>
                <if test="tagId != null">
                    AND r.TAG_ID = #{tagId}
                </if>
                <if test="freqFlag != null">
                    AND r.FREQ_FLAG = #{freqFlag}
                </if>
                ORDER BY r.SAMPLE_TIME DESC, r.ID DESC
            )
            WHERE ROWNUM &lt;= #{limit}
            </script>
            """)
    List<HistoryDataRow> findHistory(
            @Param("deviceId") Long deviceId,
            @Param("tagId") Long tagId,
            @Param("freqFlag") Integer freqFlag,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit
    );
}
