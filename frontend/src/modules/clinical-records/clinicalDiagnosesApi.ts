import type { ClinicalDiagnosis, ClinicalDiagnosisPayload, ClinicalDiagnosisStatus } from './clinicalDiagnoses.types';

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

export async function listClinicalRecordDiagnoses(recordId: string) {
  return request<ClinicalDiagnosis[]>(`/clinical-records/${recordId}/diagnoses`);
}

export async function getClinicalDiagnosis(id: string) {
  return request<ClinicalDiagnosis>(`/clinical-diagnoses/${id}`);
}

export async function createClinicalDiagnosis(recordId: string, payload: ClinicalDiagnosisPayload) {
  return request<ClinicalDiagnosis>(`/clinical-records/${recordId}/diagnoses`, {
    method: 'POST',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalDiagnosis(id: string, payload: ClinicalDiagnosisPayload) {
  return request<ClinicalDiagnosis>(`/clinical-diagnoses/${id}`, {
    method: 'PUT',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalDiagnosisStatus(id: string, diagnosisStatus: ClinicalDiagnosisStatus) {
  return request<ClinicalDiagnosis>(`/clinical-diagnoses/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ diagnosisStatus })
  });
}

export async function listPatientDiagnoses(patientId: string) {
  return request<ClinicalDiagnosis[]>(`/patients/${patientId}/diagnoses`);
}

function normalizePayload(payload: ClinicalDiagnosisPayload) {
  return {
    ...payload,
    diagnosisCatalogId: payload.diagnosisCatalogId || null,
    diagnosisText: payload.diagnosisText.trim(),
    observations: payload.observations?.trim() || null
  };
}
