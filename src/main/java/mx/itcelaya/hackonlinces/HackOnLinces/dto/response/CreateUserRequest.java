package mx.itcelaya.hackonlinces.HackOnLinces.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;

public record CreateUserRequest(

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 120)
        String fullName,

        @Size(max = 120)
        String instituteName,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        String email,

        /*
         * Password opcional — si no se proporciona se genera uno temporal.
         * El admin puede dejarlo vacío y el usuario lo cambiará después.
         */
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
        String password,

        @NotNull(message = "El tipo de usuario es obligatorio")
        UserType userType,

        @NotNull(message = "El estado de cuenta es obligatorio")
        AccountStatus accountStatus,

        @NotNull(message = "El rol es obligatorio")
        RoleName role
) {}