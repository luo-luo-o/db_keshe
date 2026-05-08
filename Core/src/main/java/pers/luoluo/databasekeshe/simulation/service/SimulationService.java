package pers.luoluo.databasekeshe.simulation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.luoluo.databasekeshe.simulation.dto.SimulationStatusResponse;
import pers.luoluo.databasekeshe.simulation.dto.SimulationTagProfile;
import pers.luoluo.databasekeshe.simulation.mapper.SimulationMapper;

@Service
public class SimulationService {

    private static final BigDecimal DEFAULT_BASE_VALUE = new BigDecimal("50.0000");
    private static final BigDecimal NORMAL_RATIO = new BigDecimal("0.72");
    private static final BigDecimal ANOMALY_RATIO = new BigDecimal("1.18");

    private final SimulationMapper simulationMapper;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean anomalyEnabled = new AtomicBoolean(false);
    private final AtomicLong writeCount = new AtomicLong();
    private final AtomicLong alarmCount = new AtomicLong();
    private final AtomicLong taskCount = new AtomicLong();

    private volatile LocalDateTime startedAt;
    private volatile LocalDateTime lastWriteAt;

    public SimulationService(SimulationMapper simulationMapper) {
        this.simulationMapper = simulationMapper;
    }

    public SimulationStatusResponse start() {
        if (running.compareAndSet(false, true)) {
            startedAt = LocalDateTime.now();
            lastWriteAt = null;
            writeCount.set(0);
            alarmCount.set(0);
            taskCount.set(0);
        }
        return status();
    }

    public SimulationStatusResponse stop() {
        running.set(false);
        anomalyEnabled.set(false);
        return status();
    }

    public SimulationStatusResponse setAnomalyEnabled(boolean enabled) {
        anomalyEnabled.set(enabled);
        return status();
    }

    public SimulationStatusResponse status() {
        return new SimulationStatusResponse(
                running.get(),
                anomalyEnabled.get(),
                startedAt,
                lastWriteAt,
                writeCount.get(),
                alarmCount.get(),
                taskCount.get()
        );
    }

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void writeTick() {
        if (!running.get()) {
            return;
        }

        List<SimulationTagProfile> profiles = simulationMapper.findTagProfiles();
        if (profiles.isEmpty()) {
            return;
        }

        LocalDateTime sampleTime = LocalDateTime.now();
        boolean abnormal = anomalyEnabled.get();
        for (SimulationTagProfile profile : profiles) {
            BigDecimal value = nextValue(profile, abnormal);
            simulationMapper.insertRawData(
                    profile.deviceId(),
                    profile.tagId(),
                    sampleTime,
                    value,
                    abnormal ? 1 : 0,
                    abnormal ? 1 : 0
            );
            writeCount.incrementAndGet();
        }

        if (abnormal) {
            createAlarmAndTask(profiles.get(0), sampleTime);
        }

        lastWriteAt = sampleTime;
    }

    private BigDecimal nextValue(SimulationTagProfile profile, boolean abnormal) {
        BigDecimal base = profile.warnLimit() == null ? DEFAULT_BASE_VALUE : profile.warnLimit();
        BigDecimal ratio = abnormal ? ANOMALY_RATIO : NORMAL_RATIO;
        long phase = writeCount.get() % 9;
        BigDecimal drift = new BigDecimal(phase).subtract(new BigDecimal("4")).multiply(new BigDecimal("0.1200"));
        return base.multiply(ratio).add(drift).setScale(4, RoundingMode.HALF_UP);
    }

    private void createAlarmAndTask(SimulationTagProfile profile, LocalDateTime sampleTime) {
        BigDecimal startValue = nextValue(profile, true);
        Long alarmId = simulationMapper.nextAlarmId();
        simulationMapper.insertAlarm(
                alarmId,
                profile.deviceId(),
                profile.tagId(),
                "SIMULATION_LIMIT",
                "SERIOUS",
                sampleTime,
                startValue,
                startValue
        );
        alarmCount.incrementAndGet();

        simulationMapper.insertTask(alarmId, "模拟派单");
        taskCount.incrementAndGet();
    }
}
