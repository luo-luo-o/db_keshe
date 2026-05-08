package pers.luoluo.databasekeshe.security;

import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pers.luoluo.databasekeshe.auth.domain.SysUser;
import pers.luoluo.databasekeshe.auth.exception.AuthException;
import pers.luoluo.databasekeshe.auth.mapper.AuthMapper;

@Service
public class AccessGuard {

    private static final int ACTIVE_STATUS = 0;

    private final AuthMapper authMapper;

    public AccessGuard(AuthMapper authMapper) {
        this.authMapper = authMapper;
    }

    public AuthenticatedUser requireUser(Long userId, String roleCode) {
        if (userId == null || roleCode == null || roleCode.isBlank()) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "请先登录");
        }

        RoleCode parsedRoleCode = parseRoleCode(roleCode);
        SysUser user = authMapper.findById(userId);
        if (user == null || !Integer.valueOf(ACTIVE_STATUS).equals(user.getStatus())) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "登录用户不存在或已停用");
        }

        RoleCode databaseRoleCode = parseRoleCode(user.getRoleCode());
        if (databaseRoleCode != parsedRoleCode) {
            throw new AuthException(HttpStatus.FORBIDDEN, "登录角色与账号不匹配");
        }

        return new AuthenticatedUser(user.getId(), user.getUsername(), user.getDisplayName(), databaseRoleCode);
    }

    public void requireAny(AuthenticatedUser user, RoleCode... allowedRoles) {
        if (user.isAdmin()) {
            return;
        }

        boolean allowed = Arrays.stream(allowedRoles).anyMatch(role -> role == user.roleCode());
        if (!allowed) {
            throw new AuthException(HttpStatus.FORBIDDEN, "当前角色无权访问该功能");
        }
    }

    public RoleCode parseRoleCode(String roleCode) {
        try {
            return RoleCode.valueOf(roleCode.trim().toUpperCase());
        } catch (RuntimeException exception) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "角色不合法");
        }
    }
}
