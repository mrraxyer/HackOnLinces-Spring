package mx.itcelaya.hackonlinces.HackOnLinces.dto.request;

import jakarta.validation.constraints.NotNull;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;

public record ChangeRoleRequest(

        @NotNull(message = "El rol es obligatorio")
        RoleName role
) {}