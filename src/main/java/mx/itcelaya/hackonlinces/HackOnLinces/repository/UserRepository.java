package mx.itcelaya.hackonlinces.HackOnLinces.repository;

import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /*
     * Carga el usuario con sus roles en una sola query (JOIN FETCH).
     * Evita N+1 al construir el UserDetails para Spring Security.
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userRoles ur LEFT JOIN FETCH ur.role WHERE u.email = :email")
    Optional<User> findByEmailWithRoles(@Param("email") String email);

    /*
     * Waitlist: usuarios externos pendientes de aprobación.
     */
    List<User> findByUserTypeAndAccountStatus(UserType userType, AccountStatus accountStatus);

    // ── Métricas para dashboard ──────────────────────────────────────────────

    long countByAccountStatus(AccountStatus accountStatus);

    long countByUserType(UserType userType);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :from")
    long countRegisteredSince(@Param("from") LocalDateTime from);

    @Query("SELECT COUNT(DISTINCT ur.user) FROM UserRole ur WHERE ur.role.name = :roleName")
    long countByRoleName(@Param("roleName") RoleName roleName);

    // ── Listado con filtros opcionales para panel admin ──────────────────────

    /*
     * Todos los parámetros son opcionales.
     * Si son null la condición se omite — equivale a "sin filtro".
     * DISTINCT es necesario porque el JOIN FETCH puede duplicar filas.
     */
    @Query("""
        SELECT DISTINCT u FROM User u
        LEFT JOIN FETCH u.userRoles ur
        LEFT JOIN FETCH ur.role
        WHERE (:status   IS NULL OR u.accountStatus = :status)
          AND (:userType IS NULL OR u.userType      = :userType)
          AND (:search   IS NULL
               OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY u.createdAt DESC
        """)
    List<User> findAllWithFilters(
            @Param("status")   AccountStatus status,
            @Param("userType") UserType userType,
            @Param("search")   String search
    );
}