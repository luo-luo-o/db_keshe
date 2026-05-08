package pers.luoluo.databasekeshe.metadata.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import pers.luoluo.databasekeshe.metadata.dto.DeviceOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.DeviceTagRow;
import pers.luoluo.databasekeshe.metadata.dto.TagOptionResponse;
import pers.luoluo.databasekeshe.metadata.mapper.MetadataMapper;

@Service
public class MetadataService {

    private final MetadataMapper metadataMapper;

    public MetadataService(MetadataMapper metadataMapper) {
        this.metadataMapper = metadataMapper;
    }

    public List<DeviceOptionResponse> listDevices() {
        Map<Long, DeviceBuilder> devices = new LinkedHashMap<>();
        for (DeviceTagRow row : metadataMapper.findDeviceTagRows()) {
            DeviceBuilder builder = devices.computeIfAbsent(row.deviceId(), ignored -> new DeviceBuilder(row));
            if (row.tagId() != null) {
                builder.tags.add(new TagOptionResponse(
                        row.tagId(),
                        row.tagCode(),
                        row.tagName(),
                        row.unit(),
                        row.warnLimit(),
                        row.rateLimit()
                ));
            }
        }

        return devices.values().stream().map(DeviceBuilder::build).toList();
    }

    private static final class DeviceBuilder {

        private final DeviceTagRow row;
        private final List<TagOptionResponse> tags = new ArrayList<>();

        private DeviceBuilder(DeviceTagRow row) {
            this.row = row;
        }

        private DeviceOptionResponse build() {
            return new DeviceOptionResponse(
                    row.stationId(),
                    row.stationName(),
                    row.bayId(),
                    row.bayName(),
                    row.deviceId(),
                    row.deviceName(),
                    row.deviceType(),
                    row.status(),
                    row.currentLimit(),
                    row.tempLimit(),
                    row.tempRateLimit(),
                    tags
            );
        }
    }
}
