package pers.luoluo.databasekeshe.metadata.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import pers.luoluo.databasekeshe.metadata.dto.DeviceTagRow;

@Mapper
public interface MetadataMapper {

    @Select("""
            SELECT
                s.ID AS stationId,
                s.NAME AS stationName,
                b.ID AS bayId,
                b.NAME AS bayName,
                d.ID AS deviceId,
                d.NAME AS deviceName,
                d.DEVICE_TYPE AS deviceType,
                d.STATUS AS status,
                d.CURRENT_LIMIT AS currentLimit,
                d.TEMP_LIMIT AS tempLimit,
                d.TEMP_RATE_LIMIT AS tempRateLimit,
                t.ID AS tagId,
                t.TAG_CODE AS tagCode,
                t.TAG_NAME AS tagName,
                t.UNIT AS unit,
                t.WARN_LIMIT AS warnLimit,
                t.RATE_LIMIT AS rateLimit
            FROM DEVICE_BASE d
            JOIN BAY_BASE b ON b.ID = d.BAY_ID
            JOIN STATION_BASE s ON s.ID = b.STATION_ID
            LEFT JOIN TAG_BASE t ON t.DEVICE_ID = d.ID
            ORDER BY s.ID, b.ID, d.ID, t.ID
            """)
    List<DeviceTagRow> findDeviceTagRows();
}
