package mx.itcelaya.hackonlinces.HackOnLinces.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;

public record ReviewUserRequest(

        /*
         * Solo APPROVED o REJECTED son decisiones válidas para un usuario.
         * PENDING no puede ser asignado manualmente por un admin.
         */
        @NotNull(message = "La decisión es obligatoria")
        AccountStatus decision,

        @Size(max = 500, message = "El motivo no puede exceder 500 caracteres")
        String reason
) {}