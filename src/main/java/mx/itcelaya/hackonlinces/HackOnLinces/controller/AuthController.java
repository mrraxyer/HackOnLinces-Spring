package mx.itcelaya.hackonlinces.HackOnLinces.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.LoginRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.RegisterRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.ApiResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AuthResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /*
     * POST /api/v1/auth/register
     * 201 Created — usuario externo registrado con rol GUEST y estado PENDING.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.created(data, "Usuario registrado exitosamente. Tu cuenta está pendiente de aprobación."));
    }

    /*
     * POST /api/v1/auth/login
     * 200 OK — credenciales válidas, devuelve JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(data, "Inicio de sesión exitoso"));
    }
}