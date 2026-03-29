package mx.itcelaya.hackonlinces.HackOnLinces.mapper;

import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AdminUserResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AuthResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.UserProfileResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.WaitlistUserResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getInstituteName(),
                user.getEmail(),
                user.getUserType(),
                user.getAccountStatus(),
                extractRoleNames(user),
                user.getCreatedAt()
        );
    }

    public AuthResponse toAuthResponse(User user, String token) {
        return AuthResponse.of(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUserType(),
                user.getAccountStatus(),
                extractRoleNames(user)
        );
    }

    public WaitlistUserResponse toWaitlistResponse(User user) {
        return new WaitlistUserResponse(
                user.getId(),
                user.getFullName(),
                user.getInstituteName(),
                user.getEmail(),
                user.getUserType(),
                user.getAccountStatus(),
                extractRoleNames(user),
                user.getCreatedAt()
        );
    }

    /*
     * Vista completa para el panel de gestión de usuarios del admin.
     * totalSubmissions se pasa explícitamente porque requeriría
     * una query extra si lo obtuviéramos desde la entidad (lazy).
     */
    public AdminUserResponse toAdminUserResponse(User user, int totalSubmissions) {
        return new AdminUserResponse(
                user.getId(),
                user.getFullName(),
                user.getInstituteName(),
                user.getEmail(),
                user.getUserType(),
                user.getAccountStatus(),
                extractRoleNames(user),
                totalSubmissions,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // ── Helper privado ───────────────────────────────────────────────────────

    private List<String> extractRoleNames(User user) {
        return user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .toList();
    }
}