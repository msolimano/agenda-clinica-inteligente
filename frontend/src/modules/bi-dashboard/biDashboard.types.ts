export interface BIBarItem {
  label: string;
  value: number;
}

export interface BIAgendaKpi {
  totalAppointments: number;
  scheduledAppointments: number;
  confirmedAppointments: number;
  cancelledAppointments: number;
  noShowAppointments: number;
  blockedSlots: number;
  overbookings: number;
  occupancyRate: number;
  occupancyRateApproximate: boolean;
}

export interface BIWaitingListKpi {
  waitingListTotal: number;
  waitingListScheduled: number;
  waitingListContacted: number;
  waitingListCancelled: number;
}

export interface BIPatientKpi {
  totalPatients: number;
  activePatients: number;
  newPatientsInRange: number;
}

export interface BIClinicalKpi {
  clinicalRecordsTotal: number;
  clinicalRecordsClosed: number;
  evolutionsTotal: number;
  diagnosesTotal: number;
  prescriptionsTotal: number;
  topDiagnoses: BIBarItem[];
  topMedications: BIBarItem[];
}

export interface BIDocumentKpi {
  clinicalDocumentsTotal: number;
  documentsByType: BIBarItem[];
  deletedDocuments: number;
}

export interface BIAIKpi {
  aiAnalysesTotal: number;
  aiAnalysesCompleted: number;
  aiAnalysesFailed: number;
  aiConsentsActive: number;
}

export interface BIFHIRKpi {
  fhirBundlesGenerated: number;
  fhirResourcesAvailable: number;
}

export interface BISummary {
  from: string;
  to: string;
  professionalId: string | null;
  specialtyId: string | null;
  agenda: BIAgendaKpi;
  waitingList: BIWaitingListKpi;
  patients: BIPatientKpi;
  clinical: BIClinicalKpi;
  documents: BIDocumentKpi;
  ai: BIAIKpi;
  fhir: BIFHIRKpi;
}

export interface BIAgendaOverview {
  agenda: BIAgendaKpi;
  waitingList: BIWaitingListKpi;
  patients: BIPatientKpi;
}

export interface BIDashboardFilters {
  from?: string;
  to?: string;
  professionalId?: string;
  specialtyId?: string;
}
