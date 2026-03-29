package mx.itcelaya.hackonlinces.HackOnLinces.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.ChangeRoleRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.CreateUserRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.ReviewSubmissionRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.request.ReviewUserRequest;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.AdminUserResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.DashboardResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.SubmissionResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.dto.response.WaitlistUserResponse;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.AuthProvider;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.Role;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.Submission;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.User;
import mx.itcelaya.hackonlinces.HackOnLinces.entity.UserRole;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AccountStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.AuthProviderType;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.RoleName;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.SubmissionStatus;
import mx.itcelaya.hackonlinces.HackOnLinces.enums.UserType;
import mx.itcelaya.hackonlinces.HackOnLinces.exception.ConflictException;
import mx.itcelaya.hackonlinces.HackOnLinces.exception.ForbiddenActionException;
import mx.itcelaya.hackonlinces.HackOnLinces.exception.ResourceNotFoundException;
import mx.itcelaya.hackonlinces.HackOnLinces.mapper.SubmissionMapper;
import mx.itcelaya.hackonlinces.HackOnLinces.mapper.UserMapper;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.AuthProviderRepository;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.RoleRepository;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.SubmissionRepository;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.UserRepository;
import mx.itcelaya.hackonlinces.HackOnLinces.repository.UserRoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthProviderRepository authProviderRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionMapper submissionMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    // ── Dashboard ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        LocalDateTime startOfToday = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        LocalDateTime startOfWeek  = LocalDateTime.now().minusDays(7);

        DashboardResponse.UserStats userStats = new DashboardResponse.UserStats(
                userRepository.count(),
                userRepository.countByAccountStatus(AccountStatus.PENDING),
                userRepository.countByAccountStatus(AccountStatus.APPROVED),
                userRepository.countByAccountStatus(AccountStatus.REJECTED),
                userRepository.countByUserType(UserType.INTERNAL),
                userRepository.countByUserType(UserType.EXTERNAL),
                userRepository.countByRoleName(RoleName.ADMIN),
                userRepository.countByRoleName(RoleName.JUDGE),
                userRepository.countByRoleName(RoleName.SPEAKER),
                userRepository.countByRoleName(RoleName.PARTICIPANT),
                userRepository.countByRoleName(RoleName.GUEST),
                userRepository.countRegisteredSince(startOfToday),
                userRepository.countRegisteredSince(startOfWeek)
        );

        DashboardResponse.SubmissionStats submissionStats = new DashboardResponse.SubmissionStats(
                submissionRepository.countTotal(),
                submissionRepository.countByStatus(SubmissionStatus.PENDING),
                submissionRepository.countByStatus(SubmissionStatus.APPROVED),
                submissionRepository.countByStatus(SubmissionStatus.REJECTED),
                submissionRepository.countByStatus(SubmissionStatus.RESUBMIT_REQUIRED)
        );

        return new DashboardResponse(userStats, submissionStats);
    }

    // ── Listado de usuarios ──────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers(AccountStatus status, UserType userType, String search) {
        // Normalizar search vacío a null para que la query lo ignore
        String normalizedSearch = (search != null && search.isBlank()) ? null : search;

        return userRepository.findAllWithFilters(status, userType, normalizedSearch)
                .stream()
                .map(u -> userMapper.toAdminUserResponse(u, submissionRepository.countByUser_Id(u.getId())))
                .toList();
    }

    // ── Crear usuario manualmente ────────────────────────────────────────────

    @Transactional
    public AdminUserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Ya existe una cuenta con el email: " + request.email());
        }

        String rawPassword = (request.password() != null && !request.password().isBlank())
                ? request.password()
                : generateTemporaryPassword();

        User user = new User();
        user.setFullName(request.fullName());
        user.setInstituteName(request.instituteName());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setUserType(request.userType());
        user.setAccountStatus(request.accountStatus());
        user = userRepository.save(user);

        Role role = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.role()));
        userRoleRepository.save(new UserRole(user, role));

        AuthProvider ap = new AuthProvider();
        ap.setUser(user);
        ap.setProvider(AuthProviderType.LOCAL);
        ap.setProviderEmail(user.getEmail());
        authProviderRepository.save(ap);

        log.info("Usuario creado manualmente por admin: {} con rol {}", user.getEmail(), request.role());

        User saved = userRepository.findByEmailWithRoles(user.getEmail()).orElseThrow();
        return userMapper.toAdminUserResponse(saved, 0);
    }

    // ── Waitlist ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<WaitlistUserResponse> getWaitlist() {
        return userRepository
                .findByUserTypeAndAccountStatus(UserType.EXTERNAL, AccountStatus.PENDING)
                .stream()
                .map(userMapper::toWaitlistResponse)
                .toList();
    }

    // ── Revisión de usuario ──────────────────────────────────────────────────

    @Transactional
    public AdminUserResponse reviewUser(Long userId, ReviewUserRequest request) {
        if (request.decision() == AccountStatus.PENDING) {
            throw new ForbiddenActionException("No se puede asignar el estado PENDING manualmente");
        }

        User user = userRepository.findByEmailWithRoles(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                        .getEmail()
        ).orElseThrow();

        user.setAccountStatus(request.decision());

        if (request.decision() == AccountStatus.APPROVED) {
            promoteToParticipant(user);
        }

        userRepository.save(user);
        log.info("Usuario {} actualizado a {} por admin", user.getEmail(), request.decision());

        User updated = userRepository.findByEmailWithRoles(user.getEmail()).orElseThrow();
        return userMapper.toAdminUserResponse(updated, submissionRepository.countByUser_Id(userId));
    }

    // ── Cambio de rol ────────────────────────────────────────────────────────

    @Transactional
    public AdminUserResponse changeRole(Long userId, ChangeRoleRequest request) {
        User user = userRepository.findByEmailWithRoles(
                userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"))
                        .getEmail()
        ).orElseThrow();

        Role newRole = roleRepository.findByName(request.role())
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + request.role()));

        userRoleRepository.deleteAll(user.getUserRoles());
        userRoleRepository.save(new UserRole(user, newRole));

        log.info("Rol de usuario {} cambiado a {}", user.getEmail(), request.role());

        User updated = userRepository.findByEmailWithRoles(user.getEmail()).orElseThrow();
        return userMapper.toAdminUserResponse(updated, submissionRepository.countByUser_Id(userId));
    }

    // ── Submissions ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getPendingSubmissions() {
        return submissionRepository
                .findByStatusOrderByCreatedAtAsc(SubmissionStatus.PENDING)
                .stream()
                .map(submissionMapper::toResponse)
                .toList();
    }

    @Transactional
    public SubmissionResponse reviewSubmission(UUID submissionId, Long adminId, ReviewSubmissionRequest request) {
        if (request.decision() == SubmissionStatus.PENDING) {
            throw new ForbiddenActionException("No se puede asignar el estado PENDING manualmente");
        }

        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission no encontrada"));

        if (submission.getStatus() != SubmissionStatus.PENDING) {
            throw new ConflictException("Esta submission ya fue revisada con estado: " + submission.getStatus());
        }

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin no encontrado"));

        submission.setStatus(request.decision());
        submission.setReason(request.comment());
        submission.setReviewedBy(admin);
        submission.setReviewedAt(LocalDateTime.now());
        submissionRepository.save(submission);

        if (request.decision() == SubmissionStatus.APPROVED) {
            User user = userRepository.findByEmailWithRoles(
                    submission.getUser().getEmail()
            ).orElseThrow();

            user.setAccountStatus(AccountStatus.APPROVED);
            promoteToParticipant(user);
            userRepository.save(user);

            log.info("Submission {} aprobada — usuario {} promovido a PARTICIPANT",
                    submissionId, user.getEmail());
        }

        return submissionMapper.toResponse(submissionRepository.findById(submissionId).orElseThrow());
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private void promoteToParticipant(User user) {
        boolean alreadyParticipant = user.getUserRoles().stream()
                .anyMatch(ur -> ur.getRole().getName() == RoleName.PARTICIPANT);
        if (alreadyParticipant) return;

        Role participantRole = roleRepository.findByName(RoleName.PARTICIPANT)
                .orElseThrow(() -> new ResourceNotFoundException("Rol PARTICIPANT no encontrado"));

        user.getUserRoles().stream()
                .filter(ur -> ur.getRole().getName() == RoleName.GUEST)
                .findFirst()
                .ifPresent(userRoleRepository::delete);

        userRoleRepository.save(new UserRole(user, participantRole));
    }

    private String generateTemporaryPassword() {
        // Password temporal simple — el usuario debe cambiarlo
        return "Temp" + UUID.randomUUID().toString().substring(0, 8) + "!";
    }
}