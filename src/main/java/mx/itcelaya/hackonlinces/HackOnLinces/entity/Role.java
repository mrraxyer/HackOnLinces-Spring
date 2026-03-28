package mx.itcelaya.hackonlinces.HackOnLinces.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Persiste el nombre como STRING para que sea legible en la BD
     * y resistente a reordenamientos del enum.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true, length = 30)
    private RoleName name;

    public Role(RoleName name) {
        this.name = name;
    }
}
