package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/*
 * Wrapper estándar para TODAS las respuestas de la API.
 *
 * Regla de "Cero Nulls":
 *   - success  → siempre presente
 *   - message  → siempre presente, nunca null
 *   - data     → puede ser null en respuestas de error o acciones sin cuerpo
 *   - errors   → solo presente en errores de validación (campos inválidos)
 *   - timestamp → siempre presente
 *
 * Uso en controllers:
 *   return ApiResponse.ok(data);
 *   return ApiResponse.ok(data, "Registro exitoso");
 *   return ApiResponse.created(data, "Usuario creado");
 *   return ApiResponse.noContent("Eliminado correctamente");
 *
 * Uso en GlobalExceptionHandler:
 *   return ApiResponse.error(HttpStatus.NOT_FOUND, "Recurso no encontrado");
 *   return ApiResponse.validationError(fieldErrors);
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Object errors,
        LocalDateTime timestamp
) {

    // ── Respuestas exitosas ──────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        return new ApiResponse<>(true, message, data, null, LocalDateTime.now());
    }

    /*
     * Para acciones sin cuerpo de respuesta (ej. DELETE, aprobaciones).
     * data será null, pero message siempre presente.
     */
    public static <Void> ApiResponse<Void> noContent(String message) {
        return new ApiResponse<>(true, message, null, null, LocalDateTime.now());
    }

    // ── Respuestas de error ──────────────────────────────────────────────────

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, message, null, null, LocalDateTime.now());
    }

    /*
     * Error de validación con detalle de campos.
     * errors contiene un Map<String, String> con campo → mensaje.
     */
    public static <T> ApiResponse<T> validationError(Object fieldErrors) {
        return new ApiResponse<>(false, "Error de validación en los campos enviados", null, fieldErrors, LocalDateTime.now());
    }
}