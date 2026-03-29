package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String originalName,
        String mimeType,
        Long size,
        LocalDateTime uploadedAt
) {}