package pers.luoluo.databasekeshe.security;

public record AuthenticatedUser(
        Long userId,
        String username,
        String displayName,
        RoleCode roleCode
) {
    public boolean isAdmin() {
        return roleCode == RoleCode.ADMIN;
    }
}
