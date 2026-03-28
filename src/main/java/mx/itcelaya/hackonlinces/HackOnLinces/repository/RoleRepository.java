package mx.itcelaya.hackonlinces.HackOnLinces.repository;

import mx.itcelaya.hackonlinces.HackOnLinces.entity.Role;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}