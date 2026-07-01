package pers.luoluo.databasekeshe.simulation.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pers.luoluo.databasekeshe.security.AccessGuard;
import pers.luoluo.databasekeshe.security.AuthenticatedUser;
import pers.luoluo.databasekeshe.security.RoleCode;
import pers.luoluo.databasekeshe.simulation.dto.AnomalyToggleRequest;
import pers.luoluo.databasekeshe.simulation.dto.SimulationDataPage;
import pers.luoluo.databasekeshe.simulation.dto.SimulationStatusResponse;
import pers.luoluo.databasekeshe.simulation.service.SimulationService;

@RestController
@RequestMapping("/api/simulation")
public class SimulationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationController.class);
    private final SimulationService simulationService;
    private final AccessGuard accessGuard;

    public SimulationController(SimulationService simulationService, AccessGuard accessGuard) {
        this.simulationService = simulationService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/status")
    public SimulationStatusResponse status(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode
    ) {
        requireAdmin(userId, roleCode);
        return simulationService.status();
    }

    @GetMapping("/data")
    public SimulationDataPage data(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        requireAdmin(userId, roleCode);
        return simulationService.recentData(page, size);
    }

    @PostMapping("/start")
    public SimulationStatusResponse start(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        SimulationStatusResponse response = simulationService.start();
        LOGGER.info("event=simulation_start userId={} username={} roleCode={} running={} anomalyEnabled={} result=SUCCESS",
                user.userId(), sanitize(user.username()), user.roleCode(), response.running(), response.anomalyEnabled());
        return response;
    }

    @PostMapping("/stop")
    public SimulationStatusResponse stop(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        SimulationStatusResponse response = simulationService.stop();
        LOGGER.info("event=simulation_stop userId={} username={} roleCode={} running={} anomalyEnabled={} result=SUCCESS",
                user.userId(), sanitize(user.username()), user.roleCode(), response.running(), response.anomalyEnabled());
        return response;
    }

    @PutMapping("/anomaly")
    public SimulationStatusResponse anomaly(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @RequestBody AnomalyToggleRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        SimulationStatusResponse response = simulationService.setAnomalyEnabled(request.enabled());
        LOGGER.info("event=simulation_anomaly_toggle userId={} username={} roleCode={} enabled={} running={} result=SUCCESS",
                user.userId(), sanitize(user.username()), user.roleCode(), request.enabled(), response.running());
        return response;
    }

    private AuthenticatedUser requireAdmin(Long userId, String roleCode) {
        AuthenticatedUser user = accessGuard.requireUser(userId, roleCode);
        accessGuard.requireAny(user, RoleCode.ADMIN);
        return user;
    }

    private String sanitize(String value) {
        return value == null ? "-" : value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
