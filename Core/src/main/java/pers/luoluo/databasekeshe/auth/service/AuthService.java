package pers.luoluo.databasekeshe.auth.service;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pers.luoluo.databasekeshe.auth.domain.SysUser;
import pers.luoluo.databasekeshe.auth.dto.AuthResponse;
import pers.luoluo.databasekeshe.auth.dto.LoginRequest;
import pers.luoluo.databasekeshe.auth.dto.RegisterRequest;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.auth.mapper.AuthMapper;
import pers.luoluo.databasekeshe.logging.RequestLogContext;

@Service
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final int ACTIVE_STATUS = 0;
    private static final Set<String> ROLE_CODES = Set.of("ADMIN", "ENGINEER", "MANAGER");

    private final AuthMapper authMapper;
    private final PasswordService passwordService;

    public AuthService(AuthMapper authMapper, PasswordService passwordService) {
        this.authMapper = authMapper;
        this.passwordService = passwordService;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String usernameForLog = sanitize(request.username());
        String roleCodeForLog = "-";
        try {
            String username = requireText(request.username(), "用户名不能为空");
            String password = requireText(request.password(), "密码不能为空");
            SysUser user = authMapper.findByUsername(username);

            if (user != null) {
                roleCodeForLog = sanitize(user.getRoleCode());
            }

            if (user == null || !passwordService.matches(password, user.getPasswordHash())) {
                throw new AuthException(HttpStatus.UNAUTHORIZED, "用户名或密码错误");
            }

            if (!Integer.valueOf(ACTIVE_STATUS).equals(user.getStatus())) {
                throw new AuthException(HttpStatus.FORBIDDEN, "用户已停用");
            }

            authMapper.updateLastLoginAt(user.getId());
            LOGGER.info("event=auth_login username={} userId={} roleCode={} result=SUCCESS",
                    sanitize(user.getUsername()),
                    user.getId(),
                    sanitize(user.getRoleCode()));
            return AuthResponse.from(user);
        } catch (AuthException exception) {
            LOGGER.warn("event=auth_login username={} roleCode={} result=FAIL status={} message={}",
                    usernameForLog,
                    roleCodeForLog,
                    exception.status().value(),
                    sanitize(exception.getMessage()));
            throw exception;
        }
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String usernameForLog = sanitize(request.username());
        String roleCodeForLog = sanitize(request.roleCode());
        try {
            String username = requireText(request.username(), "用户名不能为空");
            String password = requireText(request.password(), "密码不能为空");
            String displayName = requireText(request.displayName(), "显示名称不能为空");
            String roleCode = requireRoleCode(request.roleCode());
            roleCodeForLog = sanitize(roleCode);

            if (username.length() > 64) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "用户名长度不能超过 64");
            }

            if (displayName.length() > 100) {
                throw new AuthException(HttpStatus.BAD_REQUEST, "显示名称长度不能超过 100");
            }

            if (authMapper.findByUsername(username) != null) {
                throw new AuthException(HttpStatus.CONFLICT, "用户名已存在");
            }

            try {
                authMapper.insertUser(username, passwordService.hash(password), displayName, roleCode);
            } catch (DuplicateKeyException exception) {
                throw new AuthException(HttpStatus.CONFLICT, "用户名已存在");
            }

            SysUser user = authMapper.findByUsername(username);
            if (user == null) {
                throw new AuthException(HttpStatus.INTERNAL_SERVER_ERROR, "注册失败");
            }

            LOGGER.info("event=auth_register username={} userId={} roleCode={} result=SUCCESS",
                    sanitize(user.getUsername()),
                    user.getId(),
                    sanitize(user.getRoleCode()));
            return AuthResponse.from(user);
        } catch (AuthException exception) {
            LOGGER.warn("event=auth_register username={} roleCode={} result=FAIL status={} message={}",
                    usernameForLog,
                    roleCodeForLog,
                    exception.status().value(),
                    sanitize(exception.getMessage()));
            throw exception;
        }
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, message);
        }

        return value.trim();
    }

    private String requireRoleCode(String roleCode) {
        String normalizedRoleCode = requireText(roleCode, "角色不能为空").toUpperCase();

        if (!ROLE_CODES.contains(normalizedRoleCode)) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "角色不合法");
        }

        return normalizedRoleCode;
    }

    private String sanitize(String value) {
        return RequestLogContext.sanitizeForLog(value);
    }
}
