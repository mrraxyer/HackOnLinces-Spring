package mx.itcelaya.hackonlinces.HackOnLinces.mapper;

import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AuthResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.UserProfileResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

/*
 * Mapper manual — sin librerías externas (MapStruct, ModelMapper).
 * Preferimos control explícito sobre magia implícita para un proyecto
 * donde la estructura de entidades puede evolucionar frecuentemente.
 *
 * Regla: el mapper NUNCA recibe ni devuelve entidades JPA en métodos públicos
 * que salgan del paquete service/controller. Solo trabaja con DTOs hacia afuera.
 */
@Component
public class UserMapper {

    /*
     * User → UserProfileResponse
     * Requiere que user.getUserRoles() ya esté inicializado (no lazy sin sesión).
     * Usar siempre con findByEmailWithRoles() para garantizar esto.
     */
    public UserProfileResponse toProfileResponse(User user) {
        List<String> roles = extractRoleNames(user);

        return new UserProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getInstituteName(),
                user.getEmail(),
                user.getUserType(),
                user.getAccountStatus(),
                roles,
                user.getCreatedAt()
        );
    }

    /*
     * User + token → AuthResponse
     * Se usa después del registro o login para devolver token + datos básicos.
     */
    public AuthResponse toAuthResponse(User user, String token) {
        List<String> roles = extractRoleNames(user);

        return AuthResponse.of(
                token,
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getUserType(),
                user.getAccountStatus(),
                roles
        );
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private List<String> extractRoleNames(User user) {
        return user.getUserRoles().stream()
                .map(ur -> ur.getRole().getName().name())
                .toList();
    }
}