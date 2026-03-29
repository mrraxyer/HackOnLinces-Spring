package mx.itcelaya.hackonlinces.HackOnLinces.dto.response;

/*
 * Respuesta del endpoint GET /admin/dashboard.
 * Agrupa todas las métricas en un solo objeto para que el frontend
 * haga UNA sola llamada y pinte todas las tarjetas del panel.
 */
public record DashboardResponse(
        UserStats users,
        SubmissionStats submissions
) {

    public record UserStats(
            long total,
            long pending,
            long approved,
            long rejected,
            long internal,
            long external,
            long admins,
            long judges,
            long speakers,
            long participants,
            long guests,
            long registeredToday,
            long registeredThisWeek
    ) {}

    public record SubmissionStats(
            long total,
            long pending,
            long approved,
            long rejected,
            long resubmitRequired
    ) {}
}