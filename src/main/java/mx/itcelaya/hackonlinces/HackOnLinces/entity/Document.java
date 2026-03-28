package mx.itcelaya.hackonlinces.HackOnLinces.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "documents",
        indexes = {
                @Index(name = "idx_documents_submission_id", columnList = "submission_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /*
     * Cada documento pertenece a exactamente una Submission.
     * Si la submission se elimina, sus documentos también (via cascade en Submission).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    /*
     * path: ruta en disco o en el sistema de almacenamiento (S3, volumen, etc.)
     * Se guarda la ruta relativa para que sea portátil entre entornos.
     */
    @Column(name = "path", nullable = false, length = 512)
    private String path;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    /*
     * size en bytes. Long para soportar archivos grandes sin overflow.
     */
    @Column(name = "size", nullable = false)
    private Long size;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;
}
