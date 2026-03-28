package mx.itcelaya.hackonlinces.HackOnLinces.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.UserRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no es Bearer, dejamos pasar sin autenticar.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Solo autenticamos si aún no hay autenticación en el contexto.
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String userId = jwtUtil.extractUserId(token);

            /*
             * Usamos findByEmailWithRoles para traer roles en una sola query.
             * Buscamos por ID (del claim "sub") para evitar depender del email
             * si el usuario lo cambia en el futuro.
             */
            userRepository.findById(Long.parseLong(userId))
                    .ifPresent(user -> setAuthentication(user, request));
        }

        filterChain.doFilter(request, response);
    }

    private void setAuthentication(User user, HttpServletRequest request) {
        /*
         * Cargamos los roles del usuario con una query adicional aquí.
         * Alternativa futura: extraer roles directo del JWT para evitar esta query.
         */
        userRepository.findByEmailWithRoles(user.getEmail()).ifPresent(userWithRoles -> {
            AppUserDetails userDetails = new AppUserDetails(userWithRoles);
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        });
    }
}