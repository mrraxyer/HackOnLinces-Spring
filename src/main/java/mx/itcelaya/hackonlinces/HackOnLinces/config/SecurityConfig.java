package mx.itcelaya.hackonlinces.HackOnLinces.config;

import lombok.RequiredArgsConstructor;
import mx.itcelaya.hackonlinces.HackOnLinces.security.AppUserDetailsService;
import mx.itcelaya.hackonlinces.HackOnLinces.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Habilita @PreAuthorize en controllers y services
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AppUserDetailsService userDetailsService;
    private final mx.itcelaya.hackonlinces.HackOnLinces.security.OAuth2SuccessHandler oauth2SuccessHandler;

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    /*
     * Rutas completamente públicas — no requieren ningún token ni sesión.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/**",           // Cubre /register y /login
            "/oauth2/**",         // Flujo de autorización
            "/login/oauth2/**",   // Callbacks de Google
            "/v3/api-docs/**",    // Documentación OpenAPI
            "/swagger-ui/**",     // Interfaz de Swagger
            "/swagger-ui.html",   // Punto de entrada de Swagger
            "/actuator/**",       // Hace público el health check
            "/error"              // Evita redirecciones infinitas si algo falla
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF deshabilitado: usamos JWT (stateless), no cookies de sesión.
                .csrf(AbstractHttpConfigurer::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()

                        // Rutas de admin — solo ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated()
                )

                /*
                 * Configuración del flujo OAuth2 con Google.
                 * El successHandler lo implementaremos en la Fase 3 (OAuth + JWT).
                 */
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(ep ->
                                ep.baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(ep ->
                                ep.baseUri("/login/oauth2/code/*")
                        )
                        .successHandler(oauth2SuccessHandler)
                )

                .authenticationProvider(authenticationProvider())

                // El filtro JWT se ejecuta antes del filtro de autenticación estándar.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Convertimos la cadena CSV de orígenes a lista
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins);

        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}