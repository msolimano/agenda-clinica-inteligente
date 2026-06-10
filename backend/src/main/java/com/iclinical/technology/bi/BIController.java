package com.iclinical.technology.bi;

import com.iclinical.technology.bi.dto.BIAIKpiResponse;
import com.iclinical.technology.bi.dto.BIAITrendResponse;
import com.iclinical.technology.bi.dto.BIAgendaResponse;
import com.iclinical.technology.bi.dto.BIAppointmentTrendResponse;
import com.iclinical.technology.bi.dto.BIBarItemResponse;
import com.iclinical.technology.bi.dto.BIClinicalKpiResponse;
import com.iclinical.technology.bi.dto.BIComparisonResponse;
import com.iclinical.technology.bi.dto.BIDocumentKpiResponse;
import com.iclinical.technology.bi.dto.BIEfficiencyResponse;
import com.iclinical.technology.bi.dto.BIFHIRKpiResponse;
import com.iclinical.technology.bi.dto.BIProfessionalRankingResponse;
import com.iclinical.technology.bi.dto.BISpecialtyOccupancyResponse;
import com.iclinical.technology.bi.dto.BISpecialtyRankingResponse;
import com.iclinical.technology.bi.dto.BISummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bi")
public class BIController {

    private final BIService biService;

    public BIController(BIService biService) {
        this.biService = biService;
    }

    @GetMapping("/summary")
    public BISummaryResponse summary(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.summary(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/agenda")
    public BIAgendaResponse agenda(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.agendaOverview(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/clinical")
    public BIClinicalKpiResponse clinical(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.clinical(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/documents")
    public BIDocumentKpiResponse documents(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return biService.documents(filter(from, to, null, null));
    }

    @GetMapping("/ai")
    public BIAIKpiResponse ai(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return biService.ai(filter(from, to, null, null));
    }

    @GetMapping("/fhir")
    public BIFHIRKpiResponse fhir(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return biService.fhir(filter(from, to, null, null));
    }

    @GetMapping("/trends/appointments")
    public BIAppointmentTrendResponse appointmentTrend(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.appointmentTrend(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/specialties/occupancy")
    public List<BISpecialtyOccupancyResponse> specialtyOccupancy(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.specialtyOccupancy(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/clinical/top-diagnoses")
    public List<BIBarItemResponse> topDiagnoses(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.topDiagnoses(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/clinical/top-medications")
    public List<BIBarItemResponse> topMedications(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.topMedications(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/ai/trends")
    public BIAITrendResponse aiTrends(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return biService.aiTrends(filter(from, to, null, null));
    }

    @GetMapping("/comparison")
    public BIComparisonResponse comparison(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.comparison(filter(from, to, professionalId, specialtyId));
    }

    @GetMapping("/rankings/specialties")
    public List<BISpecialtyRankingResponse> specialtyRankings(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to
    ) {
        return biService.specialtyRankings(filter(from, to, null, null));
    }

    @GetMapping("/rankings/professionals")
    public List<BIProfessionalRankingResponse> professionalRankings(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.professionalRankings(filter(from, to, null, specialtyId));
    }

    @GetMapping("/efficiency")
    public BIEfficiencyResponse efficiency(
        @RequestParam(required = false) Instant from,
        @RequestParam(required = false) Instant to,
        @RequestParam(required = false) UUID professionalId,
        @RequestParam(required = false) UUID specialtyId
    ) {
        return biService.efficiency(filter(from, to, professionalId, specialtyId));
    }

    private BIFilter filter(Instant from, Instant to, UUID professionalId, UUID specialtyId) {
        return BIFilter.of(from, to, professionalId, specialtyId);
    }
}
