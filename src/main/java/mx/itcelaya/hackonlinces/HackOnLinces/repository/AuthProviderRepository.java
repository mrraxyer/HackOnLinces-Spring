package mx.itcelaya.hackonlinces.HackOnLinces.repository;

import mx.itcelaya.hackonlinces.HackOnLinces.entity.AuthProvider;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthProviderRepository extends JpaRepository<AuthProvider, Long> {

    /*
     * Busca si ya existe un registro de un proveedor específico
     * para ese usuario — para evitar duplicados en el flujo OAuth.
     */
    Optional<AuthProvider> findByUser_IdAndProvider(Long userId, AuthProviderType provider);

    /*
     * Busca por el ID externo del proveedor (ej. el "sub" de Google).
     * Permite encontrar al usuario cuando llega el callback OAuth.
     */
    Optional<AuthProvider> findByProviderAndProviderUserId(AuthProviderType provider, String providerUserId);
}