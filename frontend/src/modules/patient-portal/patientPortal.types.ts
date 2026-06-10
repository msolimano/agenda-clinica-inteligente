export interface PatientPortalPatient {
  id: string;
  organizationId: string;
  documentType: string | null;
  documentNumber: string | null;
  firstName: string;
  lastName: string;
  birthDate: string | null;
  sex: string | null;
  email: string | null;
  phone: string | null;
  address: string | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
  emergencyContactRelationship: string | null;
  status: string;
  createdAt: string;
  updatedAt: string;
}

export interface PatientPortalAIConsent {
  id: string | null;
  active: boolean;
  consentType: string;
  consentVersion: string | null;
  grantedAt: string | null;
  revokedAt: string | null;
  status: string;
}

export interface PatientPortalSummary {
  patient: PatientPortalPatient;
  upcomingAppointments: number;
  historicalAppointments: number;
  activeDocuments: number;
  activePrescriptions: number;
  activeDiagnoses: number;
  clinicalRecords: number;
  aiConsent: PatientPortalAIConsent;
}

export interface PatientPortalAppointment {
  id: string;
  professionalId: string;
  professionalName: string;
  startAt: string;
  endAt: string;
  appointmentType: string;
  status: string;
  reason: string | null;
  upcoming: boolean;
}

export interface PatientPortalDocument {
  id: string;
  clinicalRecordId: string | null;
  documentType: string;
  title: string | null;
  description: string | null;
  fileName: string;
  mimeType: string;
  fileSize: number;
  aiAnalysisStatus: string;
  status: string;
  downloadUrl: string;
  createdAt: string;
}

export interface PatientPortalPrescription {
  id: string;
  clinicalRecordId: string;
  diagnosisId: string | null;
  diagnosisText: string | null;
  professionalName: string;
  medicationName: string;
  dosage: string;
  frequency: string;
  duration: string | null;
  route: string | null;
  patientInstructions: string | null;
  clinicalNotes: string | null;
  prescriptionStatus: string;
  historical: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PatientPortalDiagnosis {
  id: string;
  clinicalRecordId: string;
  professionalName: string;
  diagnosisText: string;
  primary: boolean;
  diagnosisStatus: string;
  displayStatus: string;
  observations: string | null;
  codeSystem: string | null;
  diagnosisCode: string | null;
  diagnosisCodeDisplay: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface PatientPortalEvolution {
  id: string;
  clinicalRecordId: string;
  professionalName: string;
  evolutionDate: string;
  subjective: string | null;
  objective: string | null;
  assessment: string | null;
  plan: string | null;
  notes: string | null;
  evolutionStatus: string;
  cancelled: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PatientPortalClinicalRecord {
  id: string;
  professionalId: string;
  professionalName: string;
  appointmentId: string | null;
  recordDate: string;
  chiefComplaint: string | null;
  assessment: string | null;
  plan: string | null;
  status: string;
  evolutions: PatientPortalEvolution[];
}

export interface PatientPortalClinicalHistory {
  records: PatientPortalClinicalRecord[];
}

export interface PatientPortalData {
  summary: PatientPortalSummary;
  appointments: PatientPortalAppointment[];
  documents: PatientPortalDocument[];
  prescriptions: PatientPortalPrescription[];
  diagnoses: PatientPortalDiagnosis[];
  clinicalHistory: PatientPortalClinicalHistory;
  aiConsent: PatientPortalAIConsent;
}
