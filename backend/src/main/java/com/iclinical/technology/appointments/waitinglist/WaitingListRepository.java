package com.iclinical.technology.appointments.waitinglist;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WaitingListRepository extends JpaRepository<WaitingListEntry, UUID> {

    List<WaitingListEntry> findByStatusNotOrderByPriorityAscCreatedAtAsc(String status);
}
