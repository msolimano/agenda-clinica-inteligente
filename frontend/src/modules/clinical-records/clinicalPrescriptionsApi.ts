import type { ClinicalPrescription, ClinicalPrescriptionPayload, ClinicalPrescriptionStatus } from './clinicalPrescriptions.types';

const API_BASE = '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers
    },
    ...options
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => null);
    throw new Error(payload?.message ?? 'No fue posible completar la solicitud');
  }

  return response.json() as Promise<T>;
}

export async function listClinicalRecordPrescriptions(recordId: string) {
  return request<ClinicalPrescription[]>(`/clinical-records/${recordId}/prescriptions`);
}

export async function getClinicalPrescription(id: string) {
  return request<ClinicalPrescription>(`/clinical-prescriptions/${id}`);
}

export async function createClinicalPrescription(recordId: string, payload: ClinicalPrescriptionPayload) {
  return request<ClinicalPrescription>(`/clinical-records/${recordId}/prescriptions`, {
    method: 'POST',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalPrescription(id: string, payload: ClinicalPrescriptionPayload) {
  return request<ClinicalPrescription>(`/clinical-prescriptions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalPrescriptionStatus(id: string, prescriptionStatus: ClinicalPrescriptionStatus) {
  return request<ClinicalPrescription>(`/clinical-prescriptions/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ prescriptionStatus })
  });
}

export async function listPatientPrescriptions(patientId: string) {
  return request<ClinicalPrescription[]>(`/patients/${patientId}/prescriptions`);
}

function normalizePayload(payload: ClinicalPrescriptionPayload) {
  return {
    ...payload,
    diagnosisId: payload.diagnosisId?.trim() || null,
    medicationCatalogId: payload.medicationCatalogId?.trim() || null,
    medicationName: payload.medicationName.trim(),
    dosage: payload.dosage.trim(),
    frequency: payload.frequency.trim(),
    duration: payload.duration?.trim() || null,
    route: payload.route?.trim() || null,
    patientInstructions: payload.patientInstructions?.trim() || null,
    clinicalNotes: payload.clinicalNotes?.trim() || null
  };
}
