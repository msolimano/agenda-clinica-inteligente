package com.iclinical.technology.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AIAnalysisRepository extends JpaRepository<AIAnalysis, UUID> {

    List<AIAnalysis> findByClinicalDocumentIdAndStatusNotOrderByCreatedAtDesc(UUID clinicalDocumentId, String status);

    @Query("""
        select count(a) > 0
        from AIAnalysis a
        where a.clinicalDocumentId = :clinicalDocumentId
          and a.status in :statuses
        """)
    boolean existsActiveForDocument(@Param("clinicalDocumentId") UUID clinicalDocumentId, @Param("statuses") Collection<String> statuses);

    @Query("""
        select a
        from AIAnalysis a
        where a.status <> 'deleted'
          and (:status is null or a.status = :status)
          and (:patientId is null or a.patientId = :patientId)
          and (:documentId is null or a.clinicalDocumentId = :documentId)
        order by a.createdAt desc
        """)
    List<AIAnalysis> findFiltered(@Param("status") String status, @Param("patientId") UUID patientId, @Param("documentId") UUID documentId);
}
