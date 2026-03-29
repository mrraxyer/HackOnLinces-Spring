package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

import mx.itcelaya.hackonlinces.HackOnLinces.enums.SubmissionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubmissionResponse(
        UUID id,
        Long userId, // Usamos Long asumiendo que el User ID es autoincremental
        Integer attemptNumber,
        SubmissionStatus status,
        String reason,
        Long reviewedById, // ID del admin que revisó, puede ser null
        LocalDateTime reviewedAt,
        LocalDateTime sentAt,
        LocalDateTime createdAt,
        List<DocumentResponse> documents
) {}