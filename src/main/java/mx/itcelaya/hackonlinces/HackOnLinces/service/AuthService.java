package mx.itcelaya.hackonlinces.HackOnLinces.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.LoginRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.RegisterRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AuthResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.mapper.UserMapper;
import mx.itcelaya.hackonlinces.HackOnLinces.security.AppUserDetails;
import mx.itcelaya.hackonlinces.HackOnLinces.security.JwtUtil;
import mx.itcelaya.hackonlinces.HackOnLinces.service.strategy.RegistrationFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RegistrationFactory registrationFactory;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    // ── Registro ─────────────────────────────────────────────────────────────

    /*
     * El AuthService no sabe si el usuario es INTERNAL o EXTERNAL.
     * Solo le pide a la Factory la estrategia correcta y ejecuta.
     * Toda la lógica de creación de User, UserRole y AuthProvider
     * vive en la estrategia correspondiente.
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        User user = registrationFactory
                .resolve(request.email())
                .register(request);

        String token = jwtUtil.generateToken(user);
        log.info("Registro completado para: {} (type={})", user.getEmail(), user.getUserType());
        return userMapper.toAuthResponse(user, token);
    }

    // ── Login local ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        AppUserDetails userDetails = (AppUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        String token = jwtUtil.generateToken(user);
        log.info("Login local exitoso: {} (id={})", user.getEmail(), user.getId());
        return userMapper.toAuthResponse(user, token);
    }
}