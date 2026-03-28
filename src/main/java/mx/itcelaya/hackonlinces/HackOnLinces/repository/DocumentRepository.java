package mx.itcelaya.hackonlinces.HackOnLinces.repository;

import mx.itcelaya.hackonlinces.HackOnLinces.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {

    List<Document> findBySubmission_Id(UUID submissionId);
}