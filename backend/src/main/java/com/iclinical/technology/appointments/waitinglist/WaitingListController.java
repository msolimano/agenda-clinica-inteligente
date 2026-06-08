package com.iclinical.technology.appointments.waitinglist;

import com.iclinical.technology.appointments.waitinglist.dto.WaitingListCreateRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListRecommendationResponse;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListResponse;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListScheduleRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListStatusRequest;
import com.iclinical.technology.appointments.waitinglist.dto.WaitingListUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/waiting-list")
public class WaitingListController {

    private final WaitingListService waitingListService;

    public WaitingListController(WaitingListService waitingListService) {
        this.waitingListService = waitingListService;
    }

    @GetMapping
    public List<WaitingListResponse> list(
        @RequestParam(required = false) UUID specialtyId,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) String status
    ) {
        return waitingListService.list(specialtyId, professionalId, status);
    }

    @GetMapping("/{id}")
    public WaitingListResponse getById(@PathVariable UUID id) {
        return waitingListService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitingListResponse create(@Valid @RequestBody WaitingListCreateRequest request) {
        return waitingListService.create(request);
    }

    @PutMapping("/{id}")
    public WaitingListResponse update(@PathVariable UUID id, @Valid @RequestBody WaitingListUpdateRequest request) {
        return waitingListService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    public WaitingListResponse updateStatus(@PathVariable UUID id, @RequestBody WaitingListStatusRequest request) {
        return waitingListService.updateStatus(id, request);
    }

    @GetMapping("/recommendations")
    public List<WaitingListRecommendationResponse> recommendations(
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startAt,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endAt
    ) {
        return waitingListService.recommend(professionalId, specialtyId, startAt, endAt);
    }

    @PostMapping("/{id}/schedule")
    public WaitingListResponse schedule(@PathVariable UUID id, @Valid @RequestBody WaitingListScheduleRequest request) {
        return waitingListService.schedule(id, request);
    }
}
