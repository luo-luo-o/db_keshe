package pers.luoluo.databasekeshe.metadata.service;

import java.sql.Types;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.CallableStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.metadata.dto.CircuitOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.CreateCircuitRequest;
import pers.luoluo.databasekeshe.metadata.dto.CreateMeasurePointRequest;
import pers.luoluo.databasekeshe.metadata.dto.CreateTransformerRequest;
import pers.luoluo.databasekeshe.metadata.dto.MeasurePointOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.TransformerOptionResponse;
import pers.luoluo.databasekeshe.metadata.dto.UpdateCircuitRequest;
import pers.luoluo.databasekeshe.metadata.dto.UpdateMeasurePointRequest;
import pers.luoluo.databasekeshe.metadata.dto.UpdateTransformerRequest;

@Service
public class MetadataAdminService {

    private static final Pattern ORACLE_ERROR_PATTERN = Pattern.compile("ORA-\\d+:\\s*([^\\r\\n]+)");

    private final JdbcTemplate jdbcTemplate;
    private final MetadataService metadataService;

    public MetadataAdminService(JdbcTemplate jdbcTemplate, MetadataService metadataService) {
        this.jdbcTemplate = jdbcTemplate;
        this.metadataService = metadataService;
    }

    public TransformerOptionResponse createTransformer(CreateTransformerRequest request) {
        Long transformerId = executeCreate("{call PKG_PSM_ASSET.CREATE_TRANSFORMER(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", statement -> {
            statement.setString(1, normalizedText(request.transformerCode()));
            statement.setString(2, normalizedText(request.transformerName()));
            statement.setBigDecimal(3, request.ratedCapacityKva());
            statement.setString(4, normalizedText(request.ratedVoltageRatio()));
            if (request.commissionDate() == null) {
                statement.setNull(5, Types.DATE);
            } else {
                statement.setObject(5, request.commissionDate());
            }
            statement.setString(6, normalizedText(request.manufacturer()));
            statement.setBigDecimal(7, request.oilLevel());
            statement.setString(8, normalizedText(request.location()));
            setNullableInteger(statement, 9, request.status());
            statement.registerOutParameter(10, Types.NUMERIC);
        }, 10);
        return requireTransformer(transformerId);
    }

    public TransformerOptionResponse updateTransformer(Long transformerId, UpdateTransformerRequest request) {
        executeVoid("{call PKG_PSM_ASSET.UPDATE_TRANSFORMER(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", statement -> {
            statement.setLong(1, transformerId);
            statement.setString(2, normalizedText(request.transformerCode()));
            statement.setString(3, normalizedText(request.transformerName()));
            statement.setBigDecimal(4, request.ratedCapacityKva());
            statement.setString(5, normalizedText(request.ratedVoltageRatio()));
            if (request.commissionDate() == null) {
                statement.setNull(6, Types.DATE);
            } else {
                statement.setObject(6, request.commissionDate());
            }
            statement.setString(7, normalizedText(request.manufacturer()));
            statement.setBigDecimal(8, request.oilLevel());
            statement.setString(9, normalizedText(request.location()));
            setNullableInteger(statement, 10, request.status());
        });
        return requireTransformer(transformerId);
    }

    public void deleteTransformer(Long transformerId) {
        executeVoid("{call PKG_PSM_ASSET.DELETE_TRANSFORMER(?)}", statement -> statement.setLong(1, transformerId));
    }

    public CircuitOptionResponse createCircuit(Long transformerId, CreateCircuitRequest request) {
        Long circuitId = executeCreate("{call PKG_PSM_ASSET.CREATE_CIRCUIT(?, ?, ?, ?, ?, ?, ?, ?)}", statement -> {
            statement.setLong(1, transformerId);
            statement.setString(2, normalizedText(request.circuitCode()));
            statement.setString(3, normalizedText(request.circuitName()));
            statement.setString(4, normalizedText(request.direction()));
            statement.setBigDecimal(5, request.ratedVoltageKv());
            statement.setBigDecimal(6, request.ratedCurrentA());
            setNullableInteger(statement, 7, request.status());
            statement.registerOutParameter(8, Types.NUMERIC);
        }, 8);
        return requireCircuit(circuitId);
    }

    public CircuitOptionResponse updateCircuit(Long circuitId, UpdateCircuitRequest request) {
        executeVoid("{call PKG_PSM_ASSET.UPDATE_CIRCUIT(?, ?, ?, ?, ?, ?, ?)}", statement -> {
            statement.setLong(1, circuitId);
            statement.setString(2, normalizedText(request.circuitCode()));
            statement.setString(3, normalizedText(request.circuitName()));
            statement.setString(4, normalizedText(request.direction()));
            statement.setBigDecimal(5, request.ratedVoltageKv());
            statement.setBigDecimal(6, request.ratedCurrentA());
            setNullableInteger(statement, 7, request.status());
        });
        return requireCircuit(circuitId);
    }

    public void deleteCircuit(Long circuitId) {
        executeVoid("{call PKG_PSM_ASSET.DELETE_CIRCUIT(?)}", statement -> statement.setLong(1, circuitId));
    }

    public MeasurePointOptionResponse createPoint(Long transformerId, CreateMeasurePointRequest request) {
        Long pointId = executeCreate("{call PKG_PSM_ASSET.CREATE_POINT(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", statement -> {
            statement.setLong(1, transformerId);
            if (request.circuitId() == null) {
                statement.setNull(2, Types.NUMERIC);
            } else {
                statement.setLong(2, request.circuitId());
            }
            statement.setString(3, normalizedText(request.pointCode()));
            statement.setString(4, normalizedText(request.pointName()));
            statement.setString(5, normalizedText(request.pointGroup()));
            statement.setString(6, normalizedText(request.measureType()));
            statement.setString(7, normalizedText(request.phaseCode()));
            statement.setString(8, normalizedText(request.unit()));
            statement.setBigDecimal(9, request.minLimit());
            statement.setBigDecimal(10, request.maxLimit());
            statement.setBigDecimal(11, request.rateLimit());
            setNullableInteger(statement, 12, request.status());
            statement.registerOutParameter(13, Types.NUMERIC);
        }, 13);
        return requirePoint(pointId);
    }

    public MeasurePointOptionResponse updatePoint(Long pointId, UpdateMeasurePointRequest request) {
        executeVoid("{call PKG_PSM_ASSET.UPDATE_POINT(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}", statement -> {
            statement.setLong(1, pointId);
            if (request.circuitId() == null) {
                statement.setNull(2, Types.NUMERIC);
            } else {
                statement.setLong(2, request.circuitId());
            }
            statement.setString(3, normalizedText(request.pointCode()));
            statement.setString(4, normalizedText(request.pointName()));
            statement.setString(5, normalizedText(request.pointGroup()));
            statement.setString(6, normalizedText(request.measureType()));
            statement.setString(7, normalizedText(request.phaseCode()));
            statement.setString(8, normalizedText(request.unit()));
            statement.setBigDecimal(9, request.minLimit());
            statement.setBigDecimal(10, request.maxLimit());
            statement.setBigDecimal(11, request.rateLimit());
            setNullableInteger(statement, 12, request.status());
        });
        return requirePoint(pointId);
    }

    public void deletePoint(Long pointId) {
        executeVoid("{call PKG_PSM_ASSET.DELETE_POINT(?)}", statement -> statement.setLong(1, pointId));
    }

    private Long executeCreate(String sql, StatementBinder binder, int outIndex) {
        try {
            return jdbcTemplate.execute((CallableStatementCreator) connection -> {
                var statement = connection.prepareCall(sql);
                binder.bind(statement);
                return statement;
            }, (CallableStatementCallback<Long>) statement -> {
                statement.execute();
                return statement.getLong(outIndex);
            });
        } catch (DataAccessException exception) {
            throw translateException(exception);
        }
    }

    private void executeVoid(String sql, StatementBinder binder) {
        try {
            jdbcTemplate.execute((CallableStatementCreator) connection -> {
                var statement = connection.prepareCall(sql);
                binder.bind(statement);
                return statement;
            }, (CallableStatementCallback<Void>) statement -> {
                statement.execute();
                return null;
            });
        } catch (DataAccessException exception) {
            throw translateException(exception);
        }
    }

    private TransformerOptionResponse requireTransformer(Long transformerId) {
        return metadataService.listTransformers().stream()
                .filter(transformer -> transformer.transformerId().equals(transformerId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Transformer metadata refresh failed."));
    }

    private CircuitOptionResponse requireCircuit(Long circuitId) {
        return metadataService.listTransformers().stream()
                .flatMap(transformer -> transformer.circuits().stream())
                .filter(circuit -> circuit.circuitId().equals(circuitId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Circuit metadata refresh failed."));
    }

    private MeasurePointOptionResponse requirePoint(Long pointId) {
        return metadataService.listTransformers().stream()
                .flatMap(transformer -> flattenPoints(transformer).stream())
                .filter(point -> point.id().equals(pointId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Measure point metadata refresh failed."));
    }

    private List<MeasurePointOptionResponse> flattenPoints(TransformerOptionResponse transformer) {
        return java.util.stream.Stream.concat(
                transformer.points().stream(),
                transformer.circuits().stream().flatMap(circuit -> circuit.points().stream())
        ).toList();
    }

    private void setNullableInteger(java.sql.CallableStatement statement, int index, Integer value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.NUMERIC);
        } else {
            statement.setInt(index, value);
        }
    }

    private String normalizedText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private AuthException translateException(DataAccessException exception) {
        return new AuthException(HttpStatus.BAD_REQUEST, extractOracleMessage(exception));
    }

    private String extractOracleMessage(DataAccessException exception) {
        String rawMessage = exception.getMostSpecificCause() == null
                ? exception.getMessage()
                : exception.getMostSpecificCause().getMessage();
        if (rawMessage == null || rawMessage.isBlank()) {
            return "Asset operation failed.";
        }

        Matcher matcher = ORACLE_ERROR_PATTERN.matcher(rawMessage);
        while (matcher.find()) {
            String candidate = matcher.group(1).trim();
            if (!candidate.startsWith("at ")) {
                return candidate;
            }
        }
        return rawMessage.trim();
    }

    @FunctionalInterface
    private interface StatementBinder {
        void bind(java.sql.CallableStatement statement) throws java.sql.SQLException;
    }
}
