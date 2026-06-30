package pers.luoluo.databasekeshe.common.time;

import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class DatabaseTimeOffsetService {

    private final ZoneId applicationZone;
    private final ZoneId databaseZone;

    public DatabaseTimeOffsetService(
            @Value("${app.time.application-zone:Asia/Shanghai}") String applicationZone,
            @Value("${app.time.database-zone:Asia/Shanghai}") String databaseZone
    ) {
        this.applicationZone = ZoneId.of(applicationZone);
        this.databaseZone = ZoneId.of(databaseZone);
    }

    public LocalDateTime toDatabaseTime(LocalDateTime applicationTime) {
        if (applicationTime == null) {
            return null;
        }
        return applicationTime.atZone(applicationZone).withZoneSameInstant(databaseZone).toLocalDateTime();
    }

    public LocalDateTime toApplicationTime(LocalDateTime databaseTime) {
        if (databaseTime == null) {
            return null;
        }
        return databaseTime.atZone(databaseZone).withZoneSameInstant(applicationZone).toLocalDateTime();
    }

    public LocalDateTime applicationNow() {
        return LocalDateTime.now(applicationZone);
    }
}
