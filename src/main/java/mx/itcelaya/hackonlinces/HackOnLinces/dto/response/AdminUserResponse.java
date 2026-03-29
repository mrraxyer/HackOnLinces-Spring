package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;

import java.time.LocalDateTime;
import java.util.List;

/*
 * Vista de usuario para el panel de administración.
 * Incluye más información que WaitlistUserResponse
 * ya que el admin necesita ver todo para gestionar cuentas.
 */
public record AdminUserResponse(
        Long id,
        String fullName,
        String instituteName,
        String email,
        UserType userType,
        AccountStatus accountStatus,
        List<String> roles,
        int totalSubmissions,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}