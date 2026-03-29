package mx.itcelaya.hackonlinces.HackOnLinces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.ChangeRoleRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.CreateUserRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.ReviewSubmissionRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.ReviewUserRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AdminUserResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.ApiResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.DashboardResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.SubmissionResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.WaitlistUserResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;
import mx.itcelaya.hackonlinces.HackOnLinces.security.AppUserDetails;
import mx.itcelaya.hackonlinces.HackOnLinces.service.AdminService;
import mx.itcelaya.hackonlinces.HackOnLinces.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final SubmissionService submissionService;

    // ── Dashboard ────────────────────────────────────────────────────────────

    /*
     * GET /api/v1/admin/dashboard
     * Métricas globales del sistema en una sola llamada.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getDashboard(),
                "Dashboard obtenido exitosamente"
        ));
    }

    // ── Gestión de usuarios ──────────────────────────────────────────────────

    /*
     * GET /api/v1/admin/users
     * Lista todos los usuarios con filtros opcionales.
     *
     * Ejemplos:
     *   GET /admin/users                              → todos
     *   GET /admin/users?status=PENDING               → solo pendientes
     *   GET /admin/users?userType=EXTERNAL            → solo externos
     *   GET /admin/users?search=juan                  → busca por nombre o email
     *   GET /admin/users?status=APPROVED&search=garcia → combinados
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAllUsers(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) UserType userType,
            @RequestParam(required = false) String search
    ) {
        List<AdminUserResponse> users = adminService.getAllUsers(status, userType, search);
        return ResponseEntity.ok(ApiResponse.ok(users, "Usuarios obtenidos exitosamente"));
    }

    /*
     * POST /api/v1/admin/users
     * Crea un usuario manualmente desde el panel de administración.
     */
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<AdminUserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request
    ) {
        AdminUserResponse response = adminService.createUser(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Usuario creado exitosamente"));
    }

    /*
     * PATCH /api/v1/admin/users/{id}/review
     * Aprueba o rechaza un usuario directamente (sin revisar submission).
     */
    @PatchMapping("/users/{id}/review")
    public ResponseEntity<ApiResponse<AdminUserResponse>> reviewUser(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUserRequest request
    ) {
        AdminUserResponse response = adminService.reviewUser(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Usuario actualizado exitosamente"));
    }

    /*
     * PATCH /api/v1/admin/users/{id}/role
     * Cambia el rol de un usuario manualmente.
     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<ApiResponse<AdminUserResponse>> changeRole(
            @PathVariable Long id,
            @Valid @RequestBody ChangeRoleRequest request
    ) {
        AdminUserResponse response = adminService.changeRole(id, request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Rol actualizado exitosamente"));
    }

    // ── Waitlist ─────────────────────────────────────────────────────────────

    /*
     * GET /api/v1/admin/waitlist
     * Atajo rápido — equivale a GET /admin/users?status=PENDING&userType=EXTERNAL
     */
    @GetMapping("/waitlist")
    public ResponseEntity<ApiResponse<List<WaitlistUserResponse>>> getWaitlist() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getWaitlist(),
                "Waitlist obtenida exitosamente"
        ));
    }

    // ── Submissions ──────────────────────────────────────────────────────────

    /*
     * GET /api/v1/admin/submissions
     * Lista todas las submissions PENDING para revisión.
     */
    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getPendingSubmissions() {
        return ResponseEntity.ok(ApiResponse.ok(
                adminService.getPendingSubmissions(),
                "Submissions pendientes obtenidas exitosamente"
        ));
    }

    /*
     * GET /api/v1/admin/submissions/{id}
     * Detalle de cualquier submission — admin puede ver todas.
     */
    @GetMapping("/submissions/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmissionById(
            @PathVariable UUID id,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                submissionService.getById(id, userDetails.getUser().getId(), true),
                "Submission obtenida exitosamente"
        ));
    }

    /*
     * PATCH /api/v1/admin/submissions/{id}/review
     * Aprueba, rechaza o solicita reenvío.
     * APPROVED → usuario pasa a APPROVED + rol PARTICIPANT automáticamente.
     */
    @PatchMapping("/submissions/{id}/review")
    public ResponseEntity<ApiResponse<SubmissionResponse>> reviewSubmission(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewSubmissionRequest request,
            @AuthenticationPrincipal AppUserDetails userDetails
    ) {
        SubmissionResponse response = adminService.reviewSubmission(
                id, userDetails.getUser().getId(), request
        );
        return ResponseEntity.ok(ApiResponse.ok(response, "Submission revisada exitosamente"));
    }
}