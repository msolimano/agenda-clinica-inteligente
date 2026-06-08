package com.iclinical.technology.appointments;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByProfessionalIdAndStartAtLessThanAndEndAtGreaterThanAndStatusNotOrderByStartAtAsc(UUID professionalId, Instant endAt, Instant startAt, String status);

    @Query("""
        select count(a) > 0
        from Appointment a
        where a.professionalId = :professionalId
          and a.status in :blockingStatuses
          and a.startAt < :endAt
          and a.endAt > :startAt
          and (:excludedId is null or a.id <> :excludedId)
        """)
    boolean existsBlockingOverlap(
        @Param("professionalId") UUID professionalId,
        @Param("startAt") Instant startAt,
        @Param("endAt") Instant endAt,
        @Param("blockingStatuses") Collection<String> blockingStatuses,
        @Param("excludedId") UUID excludedId
    );
}
