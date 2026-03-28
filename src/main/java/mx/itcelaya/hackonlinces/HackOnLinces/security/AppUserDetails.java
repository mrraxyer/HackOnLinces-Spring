package mx.itcelaya.hackonlinces.HackOnLinces.security;

import lombok.Getter;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/*
 * Adaptador entre nuestra entidad User y lo que Spring Security necesita.
 * Guardamos una referencia al User original para acceder a campos extra
 * (userType, accountStatus, id) desde el contexto de seguridad.
 */
public class AppUserDetails implements UserDetails {

    @Getter
    private final User user;

    public AppUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getUserRoles().stream()
                .map(ur -> new SimpleGrantedAuthority("ROLE_" + ur.getRole().getName().name()))
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // Spring Security usa este valor como identificador único — usamos el email.
        return user.getEmail();
    }

    /*
     * Solo consideramos una cuenta activa si está APPROVED.
     * Un usuario PENDING o REJECTED podrá autenticarse pero no acceder
     * a recursos protegidos (lo controlamos también en el filtro JWT).
     */
    @Override
    public boolean isEnabled() {
        return user.getAccountStatus() == AccountStatus.APPROVED
                || user.getAccountStatus() == AccountStatus.PENDING;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() {
        return user.getAccountStatus() != AccountStatus.REJECTED;
    }
    @Override public boolean isCredentialsNonExpired() { return true; }
}