package com.iclinical.technology.diagnosiscatalog;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface DiagnosisCatalogRepository extends JpaRepository<DiagnosisCatalog, UUID> {

    @Query("""
        select d
        from DiagnosisCatalog d
        where d.status <> 'deleted'
          and (:includeInactive = true or d.status = 'active')
          and (:organizationId is null or d.organizationId is null or d.organizationId = :organizationId)
          and (:search is null
            or lower(d.diagnosisDisplay) like concat('%', :search, '%')
            or lower(coalesce(d.diagnosisCode, '')) like concat('%', :search, '%')
            or lower(coalesce(d.category, '')) like concat('%', :search, '%'))
        order by d.diagnosisDisplay asc, d.category asc, d.diagnosisCode asc
        """)
    List<DiagnosisCatalog> findFiltered(
        @Param("organizationId") UUID organizationId,
        @Param("search") String search,
        @Param("includeInactive") boolean includeInactive
    );
}
