package mx.itcelaya.hackonlinces.HackOnLinces.repository;

import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    Optional<User> findByEmailWithRoles(String email);

    /*
     * Waitlist: usuarios externos pendientes de aprobación.
     */
    List<User> findByUserTypeAndAccountStatus(UserType userType, AccountStatus accountStatus);
}