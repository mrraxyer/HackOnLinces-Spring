package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;

import java.time.LocalDateTime;
import java.util.List;

public record WaitlistUserResponse(
        Long id,
        String fullName,
        String instituteName,
        String email,
        UserType userType,
        AccountStatus accountStatus,
        List<String> roles,
        LocalDateTime createdAt
) {}