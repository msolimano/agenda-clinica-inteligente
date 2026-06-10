import type {
  PatientPortalAIConsent,
  PatientPortalAppointment,
  PatientPortalClinicalHistory,
  PatientPortalDiagnosis,
  PatientPortalDocument,
  PatientPortalPrescription,
  PatientPortalSummary
} from './patientPortal.types';

const API_BASE = '/api';

async function request<T>(path: string): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`);

  if (!response.ok) {
    const payload = await response.json().catch(() => null);
    throw new Error(payload?.message ?? 'No fue posible completar la solicitud');
  }

  return response.json() as Promise<T>;
}

export function getPatientPortalSummary(patientId: string) {
  return request<PatientPortalSummary>(`/patient-portal/${patientId}/summary`);
}

export function getPatientPortalAppointments(patientId: string) {
  return request<PatientPortalAppointment[]>(`/patient-portal/${patientId}/appointments`);
}

export function getPatientPortalDocuments(patientId: string) {
  return request<PatientPortalDocument[]>(`/patient-portal/${patientId}/documents`);
}

export function getPatientPortalPrescriptions(patientId: string) {
  return request<PatientPortalPrescription[]>(`/patient-portal/${patientId}/prescriptions`);
}

export function getPatientPortalDiagnoses(patientId: string) {
  return request<PatientPortalDiagnosis[]>(`/patient-portal/${patientId}/diagnoses`);
}

export function getPatientPortalClinicalHistory(patientId: string) {
  return request<PatientPortalClinicalHistory>(`/patient-portal/${patientId}/clinical-history`);
}

export function getPatientPortalAIConsent(patientId: string) {
  return request<PatientPortalAIConsent>(`/patient-portal/${patientId}/ai-consent`);
}

export function patientPortalFHIRDownloadUrl(patientId: string) {
  return `${API_BASE}/patient-portal/${patientId}/fhir-bundle/download`;
}
