package mx.itcelaya.hackonlinces.HackOnLinces.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AuthProviderType;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "auth_providers",
        uniqueConstraints = {
                /*
                 * Un usuario no puede tener dos veces el mismo proveedor registrado.
                 * Esto evita duplicados si el flujo OAuth se dispara dos veces.
                 */
                @UniqueConstraint(name = "uq_auth_providers_user_provider", columnNames = {"user_id", "provider"})
        },
        indexes = {
                @Index(name = "idx_auth_providers_user_id", columnList = "user_id"),
                @Index(name = "idx_auth_providers_provider_user_id", columnList = "provider_user_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class AuthProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Relación por user_id. El email del proveedor se guarda como
     * dato auxiliar (providerEmail), NO como foreign key.
     * Esto desacopla la identidad del proveedor de la identidad del sistema.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    private AuthProviderType provider;

    /*
     * ID único del usuario en el proveedor externo (ej. Google sub).
     * Nullable para LOCAL, ya que no existe un ID externo.
     */
    @Column(name = "provider_user_id", length = 255)
    private String providerUserId;

    /*
     * Email reportado por el proveedor. Solo dato informativo/auxiliar.
     * No se usa como FK ni como identificador interno.
     */
    @Column(name = "provider_email", length = 180)
    private String providerEmail;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
