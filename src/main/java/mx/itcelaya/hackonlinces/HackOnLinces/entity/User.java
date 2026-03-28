package mx.itcelaya.hackonlinces.HackOnLinces.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.itcelaya.hackonlinces.HackOnLinces.emums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.emums.UserType;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_account_status", columnList = "account_status"),
                @Index(name = "idx_users_user_type", columnList = "user_type")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    /*
     * instituteName es opcional: un usuario externo puede no pertenecer
     * a ningún instituto. Los internos lo heredan de su correo institucional.
     */
    @Column(name = "institute_name", length = 120)
    private String instituteName;

    @Column(name = "email", nullable = false, unique = true, length = 180)
    private String email;

    /*
     * passwordHash es nullable porque los usuarios INTERNAL
     * se autentican exclusivamente por Google OAuth y no necesitan password local.
     */
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, length = 20)
    private AccountStatus accountStatus = AccountStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /*
     * mappedBy apunta al campo "user" en UserRole.
     * CascadeType.ALL + orphanRemoval garantizan que si se elimina
     * el User, sus UserRole asociados también se eliminan.
     * fetch LAZY evita cargar los roles en cada consulta de User.
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserRole> userRoles = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<AuthProvider> authProviders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Submission> submissions = new ArrayList<>();
}

