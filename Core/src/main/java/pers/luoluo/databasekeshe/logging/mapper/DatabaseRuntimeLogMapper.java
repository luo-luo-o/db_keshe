package pers.luoluo.databasekeshe.logging.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import pers.luoluo.databasekeshe.logging.dto.RuntimeLogResponse;

@Mapper
public interface DatabaseRuntimeLogMapper {

    @Select("""
            <script>
            SELECT *
            FROM (
                SELECT
                    ID AS "id",
                    'DATABASE' AS "source",
                    LEVEL_CODE AS "level",
                    MESSAGE AS "message",
                    CONTEXT AS "context",
                    CREATED_AT AS "createdAt"
                FROM DB_RUNTIME_LOG
                <choose>
                    <when test="minWeight &lt;= 10">
                        WHERE LEVEL_CODE IN ('DEBUG', 'INFO', 'WARN', 'ERROR')
                    </when>
                    <when test="minWeight &lt;= 20">
                        WHERE LEVEL_CODE IN ('INFO', 'WARN', 'ERROR')
                    </when>
                    <when test="minWeight &lt;= 30">
                        WHERE LEVEL_CODE IN ('WARN', 'ERROR')
                    </when>
                    <otherwise>
                        WHERE LEVEL_CODE = 'ERROR'
                    </otherwise>
                </choose>
                ORDER BY CREATED_AT DESC, ID DESC
            )
            WHERE ROWNUM &lt;= #{limit}
            </script>
            """)
    List<RuntimeLogResponse> findLogs(
            @Param("minWeight") int minWeight,
            @Param("limit") int limit
    );
}
