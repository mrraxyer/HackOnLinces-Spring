package mx.itcelaya.hackonlinces.HackOnLinces.security;

import lombok.RequiredArgsConstructor;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /*
     * Spring Security llama a este método durante el login local.
     * Usamos findByEmailWithRoles para traer los roles en la misma query
     * y evitar LazyInitializationException al construir las authorities.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailWithRoles(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con email: " + email
                ));
        return new AppUserDetails(user);
    }
}