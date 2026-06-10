package com.iclinical.technology.bi;

import com.iclinical.technology.bi.dto.BIAIKpiResponse;
import com.iclinical.technology.bi.dto.BIAITrendResponse;
import com.iclinical.technology.bi.dto.BIAgendaKpiResponse;
import com.iclinical.technology.bi.dto.BIAgendaResponse;
import com.iclinical.technology.bi.dto.BIAppointmentTrendResponse;
import com.iclinical.technology.bi.dto.BIBarItemResponse;
import com.iclinical.technology.bi.dto.BIClinicalKpiResponse;
import com.iclinical.technology.bi.dto.BIDocumentKpiResponse;
import com.iclinical.technology.bi.dto.BIFHIRKpiResponse;
import com.iclinical.technology.bi.dto.BIPatientKpiResponse;
import com.iclinical.technology.bi.dto.BISpecialtyOccupancyResponse;
import com.iclinical.technology.bi.dto.BISummaryResponse;
import com.iclinical.technology.bi.dto.BIWaitingListKpiResponse;
import com.iclinical.technology.bi.repository.BIAIRepository;
import com.iclinical.technology.bi.repository.BIAgendaRepository;
import com.iclinical.technology.bi.repository.BIClinicalRepository;
import com.iclinical.technology.bi.repository.BIDocumentRepository;
import com.iclinical.technology.bi.repository.BIFHIRRepository;
import com.iclinical.technology.bi.repository.BIPatientRepository;
import com.iclinical.technology.bi.repository.BIWaitingListRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BIService {

    private final BIAgendaRepository agendaRepository;
    private final BIWaitingListRepository waitingListRepository;
    private final BIPatientRepository patientRepository;
    private final BIClinicalRepository clinicalRepository;
    private final BIDocumentRepository documentRepository;
    private final BIAIRepository aiRepository;
    private final BIFHIRRepository fhirRepository;

    public BIService(
        BIAgendaRepository agendaRepository,
        BIWaitingListRepository waitingListRepository,
        BIPatientRepository patientRepository,
        BIClinicalRepository clinicalRepository,
        BIDocumentRepository documentRepository,
        BIAIRepository aiRepository,
        BIFHIRRepository fhirRepository
    ) {
        this.agendaRepository = agendaRepository;
        this.waitingListRepository = waitingListRepository;
        this.patientRepository = patientRepository;
        this.clinicalRepository = clinicalRepository;
        this.documentRepository = documentRepository;
        this.aiRepository = aiRepository;
        this.fhirRepository = fhirRepository;
    }

    @Transactional(readOnly = true)
    public BISummaryResponse summary(BIFilter filter) {
        return new BISummaryResponse(
            filter.from(),
            filter.to(),
            filter.professionalId(),
            filter.specialtyId(),
            agenda(filter),
            waitingList(filter),
            patients(filter),
            clinical(filter),
            documents(filter),
            ai(filter),
            fhir(filter)
        );
    }

    @Transactional(readOnly = true)
    public BIAgendaResponse agendaOverview(BIFilter filter) {
        return new BIAgendaResponse(agenda(filter), waitingList(filter), patients(filter));
    }

    @Transactional(readOnly = true)
    public BIAgendaKpiResponse agenda(BIFilter filter) {
        return agendaRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIWaitingListKpiResponse waitingList(BIFilter filter) {
        return waitingListRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIPatientKpiResponse patients(BIFilter filter) {
        return patientRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIClinicalKpiResponse clinical(BIFilter filter) {
        return clinicalRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIDocumentKpiResponse documents(BIFilter filter) {
        return documentRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIAIKpiResponse ai(BIFilter filter) {
        return aiRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIFHIRKpiResponse fhir(BIFilter filter) {
        return fhirRepository.load(filter);
    }

    @Transactional(readOnly = true)
    public BIAppointmentTrendResponse appointmentTrend(BIFilter filter) {
        return agendaRepository.appointmentTrend(filter);
    }

    @Transactional(readOnly = true)
    public List<BISpecialtyOccupancyResponse> specialtyOccupancy(BIFilter filter) {
        return agendaRepository.specialtyOccupancy(filter);
    }

    @Transactional(readOnly = true)
    public List<BIBarItemResponse> topDiagnoses(BIFilter filter) {
        return clinicalRepository.topDiagnoses(filter);
    }

    @Transactional(readOnly = true)
    public List<BIBarItemResponse> topMedications(BIFilter filter) {
        return clinicalRepository.topMedications(filter);
    }

    @Transactional(readOnly = true)
    public BIAITrendResponse aiTrends(BIFilter filter) {
        return aiRepository.trends(filter);
    }
}
