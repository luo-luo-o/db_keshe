package pers.luoluo.databasekeshe.metadata.controller;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pers.luoluo.databasekeshe.metadata.dto.CircuitOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.CreateCircuitRequest;
import pers.luoluo.databasekeshe.metadata.dto.CreateMeasurePointRequest;
import pers.luoluo.databasekeshe.metadata.dto.CreateTransformerRequest;
import pers.luoluo.databasekeshe.metadata.dto.MeasurePointOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.TransformerOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.UpdateCircuitRequest;
import pers.luoluo.databasekeshe.metadata.dto.UpdateMeasurePointRequest;
import pers.luoluo.databasekeshe.metadata.dto.UpdateTransformerRequest;
import pers.luoluo.databasekeshe.metadata.service.MetadataAdminService;
import pers.luoluo.databasekeshe.metadata.service.MetadataService;
import pers.luoluo.databasekeshe.security.AccessGuard;
import pers.luoluo.databasekeshe.security.AuthenticatedUser;
import pers.luoluo.databasekeshe.security.RoleCode;

@RestController
@RequestMapping("/api/metadata")
public class MetadataController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataController.class);
    private final MetadataService metadataService;
    private final MetadataAdminService metadataAdminService;
    private final AccessGuard accessGuard;

    public MetadataController(
            MetadataService metadataService,
            MetadataAdminService metadataAdminService,
            AccessGuard accessGuard
    ) {
        this.metadataService = metadataService;
        this.metadataAdminService = metadataAdminService;
        this.accessGuard = accessGuard;
    }

    @GetMapping("/transformers")
    public List<TransformerOptionResponse> transformers(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode
    ) {
        accessGuard.requireUser(userId, roleCode);
        return metadataService.listTransformers();
    }

    @PostMapping("/transformers")
    public TransformerOptionResponse createTransformer(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @RequestBody CreateTransformerRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        TransformerOptionResponse response = metadataAdminService.createTransformer(request);
        logMetadataAction(user, "transformer", response.transformerId(), "create");
        return response;
    }

    @PutMapping("/transformers/{transformerId}")
    public TransformerOptionResponse updateTransformer(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long transformerId,
            @RequestBody UpdateTransformerRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        TransformerOptionResponse response = metadataAdminService.updateTransformer(transformerId, request);
        logMetadataAction(user, "transformer", response.transformerId(), "update");
        return response;
    }

    @DeleteMapping("/transformers/{transformerId}")
    public ResponseEntity<Void> deleteTransformer(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long transformerId
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        metadataAdminService.deleteTransformer(transformerId);
        logMetadataAction(user, "transformer", transformerId, "delete");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transformers/{transformerId}/circuits")
    public CircuitOptionResponse createCircuit(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long transformerId,
            @RequestBody CreateCircuitRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        CircuitOptionResponse response = metadataAdminService.createCircuit(transformerId, request);
        logMetadataAction(user, "circuit", response.circuitId(), "create");
        return response;
    }

    @PutMapping("/circuits/{circuitId}")
    public CircuitOptionResponse updateCircuit(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long circuitId,
            @RequestBody UpdateCircuitRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        CircuitOptionResponse response = metadataAdminService.updateCircuit(circuitId, request);
        logMetadataAction(user, "circuit", response.circuitId(), "update");
        return response;
    }

    @DeleteMapping("/circuits/{circuitId}")
    public ResponseEntity<Void> deleteCircuit(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long circuitId
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        metadataAdminService.deleteCircuit(circuitId);
        logMetadataAction(user, "circuit", circuitId, "delete");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transformers/{transformerId}/points")
    public MeasurePointOptionResponse createPoint(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long transformerId,
            @RequestBody CreateMeasurePointRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        MeasurePointOptionResponse response = metadataAdminService.createPoint(transformerId, request);
        logMetadataAction(user, "point", response.id(), "create");
        return response;
    }

    @PutMapping("/points/{pointId}")
    public MeasurePointOptionResponse updatePoint(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long pointId,
            @RequestBody UpdateMeasurePointRequest request
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        MeasurePointOptionResponse response = metadataAdminService.updatePoint(pointId, request);
        logMetadataAction(user, "point", response.id(), "update");
        return response;
    }

    @DeleteMapping("/points/{pointId}")
    public ResponseEntity<Void> deletePoint(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-Role-Code") String roleCode,
            @PathVariable Long pointId
    ) {
        AuthenticatedUser user = requireAdmin(userId, roleCode);
        metadataAdminService.deletePoint(pointId);
        logMetadataAction(user, "point", pointId, "delete");
        return ResponseEntity.noContent().build();
    }

    private AuthenticatedUser requireAdmin(Long userId, String roleCode) {
        AuthenticatedUser user = accessGuard.requireUser(userId, roleCode);
        accessGuard.requireAny(user, RoleCode.ADMIN);
        return user;
    }

    private void logMetadataAction(AuthenticatedUser user, String resourceType, Long resourceId, String action) {
        LOGGER.info("event=metadata_{} userId={} username={} roleCode={} resourceType={} resourceId={} result=SUCCESS",
                action,
                user.userId(),
                sanitize(user.username()),
                user.roleCode(),
                resourceType,
                resourceId);
    }

    private String sanitize(String value) {
        return value == null ? "-" : value.replace('\r', ' ').replace('\n', ' ').trim();
    }
}
