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
    @Query("""
        select a
        from Appointment a
        where a.professionalId = :professionalId
          and a.status in :statuses
          and a.startAt < :endAt
          and a.endAt > :startAt
          and (:excludedId is null or a.id <> :excludedId)
        order by a.startAt asc
        """)
    List<Appointment> findOverlappingByProfessionalAndStatuses(
        @Param("professionalId") UUID professionalId,
        @Param("startAt") Instant startAt,
        @Param("endAt") Instant endAt,
        @Param("statuses") Collection<String> statuses,
        @Param("excludedId") UUID excludedId
    );

    @Query("""
        select a
        from Appointment a
        where a.professionalId = :professionalId
          and a.appointmentType = 'blocked_slot'
          and a.status = 'blocked'
          and a.startAt < :to
          and a.endAt > :from
        order by a.startAt asc
        """)
    List<Appointment> findActiveBlocks(
        @Param("professionalId") UUID professionalId,
        @Param("from") Instant from,
        @Param("to") Instant to
    );

    @Query("""
        select count(a) > 0
        from Appointment a
        where a.professionalId = :professionalId
          and a.appointmentType = 'blocked_slot'
          and a.status = 'blocked'
          and a.startAt < :endAt
          and a.endAt > :startAt
          and (:excludedId is null or a.id <> :excludedId)
        """)
    boolean existsBlockedSlotOverlap(
        @Param("professionalId") UUID professionalId,
        @Param("startAt") Instant startAt,
        @Param("endAt") Instant endAt,
        @Param("excludedId") UUID excludedId
    );

    @Query("""
        select count(a)
        from Appointment a
        where a.professionalId = :professionalId
          and a.overbooking = true
          and a.status in :statuses
          and a.startAt < :endAt
          and a.endAt > :startAt
        """)
    long countActiveOverbookings(
        @Param("professionalId") UUID professionalId,
        @Param("startAt") Instant startAt,
        @Param("endAt") Instant endAt,
        @Param("statuses") Collection<String> statuses
    );

}
