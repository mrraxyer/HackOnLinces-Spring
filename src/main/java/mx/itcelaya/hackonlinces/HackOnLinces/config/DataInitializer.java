package mx.itcelaya.hackonlinces.HackOnLinces.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.Role;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    /*
     * ApplicationRunner se ejecuta una vez al arrancar Spring Boot,
     * después de que el contexto está completamente inicializado.
     *
     * Insertamos los roles solo si no existen — idempotente,
     * seguro de ejecutar con ddl-auto: update o validate.
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Arrays.stream(RoleName.values()).forEach(roleName -> {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new Role(roleName));
                log.info("Rol creado: {}", roleName);
            }
        });
        log.info("DataInitializer completado — roles verificados: {}", RoleName.values().length);
    }
}