package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;

import java.util.List;

public record AuthResponse(
        String token,
        String tokenType,
        Long userId,
        String fullName,
        String email,
        UserType userType,
        AccountStatus accountStatus,
        List<String> roles
) {
    // Constructor de conveniencia para no repetir "Bearer" en cada llamada
    public static AuthResponse of(String token, Long userId, String fullName,
                                  String email, UserType userType,
                                  AccountStatus accountStatus, List<String> roles) {
        return new AuthResponse(token, "Bearer", userId, fullName, email, userType, accountStatus, roles);
    }
}