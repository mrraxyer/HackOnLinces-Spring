package mx.itcelaya.hackonlinces.HackOnLinces.repository;

import mx.itcelaya.hackonlinces.HackOnLinces.entity.Submission;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubmissionRepository extends JpaRepository<Submission, UUID> {

    List<Submission> findByUser_IdOrderByAttemptNumberAsc(Long userId);

    /*
     * Verifica si ya existe una submission PENDING para ese usuario.
     * Regla de negocio: no puede haber dos submissions activas a la vez.
     */
    boolean existsByUser_IdAndStatus(Long userId, SubmissionStatus status);

    /*
     * Cuenta los intentos del usuario — para validar el límite configurable.
     */
    int countByUser_Id(Long userId);

    /*
     * Obtiene el último número de intento del usuario para calcular el siguiente.
     */
    Optional<Submission> findTopByUser_IdOrderByAttemptNumberDesc(Long userId);

    /*
     * Lista todas las submissions con un status dado — para el panel de admin.
     */
    List<Submission> findByStatusOrderByCreatedAtAsc(SubmissionStatus status);
}