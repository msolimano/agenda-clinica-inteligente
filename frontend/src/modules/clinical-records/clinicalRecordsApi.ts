import type { ClinicalRecord, ClinicalRecordPayload, ClinicalRecordStatus, ClinicalRecordSummary, PatientOption, ProfessionalOption } from './clinicalRecords.types';

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

export async function listPatients() {
  return request<PatientOption[]>('/patients');
}

export async function listProfessionals() {
  return request<ProfessionalOption[]>('/professionals');
}

export async function listClinicalRecords(params: { patientId?: string; professionalId?: string; status?: ClinicalRecordStatus } = {}) {
  const query = new URLSearchParams();
  if (params.patientId) {
    query.set('patientId', params.patientId);
  }
  if (params.professionalId) {
    query.set('professionalId', params.professionalId);
  }
  if (params.status) {
    query.set('status', params.status);
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<ClinicalRecordSummary[]>(`/clinical-records${suffix}`);
}

export async function listPatientClinicalRecords(patientId: string) {
  return request<ClinicalRecordSummary[]>(`/patients/${patientId}/clinical-records`);
}

export async function getClinicalRecord(id: string) {
  return request<ClinicalRecord>(`/clinical-records/${id}`);
}

export async function createClinicalRecord(payload: ClinicalRecordPayload) {
  return request<ClinicalRecord>('/clinical-records', {
    method: 'POST',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalRecord(id: string, payload: ClinicalRecordPayload) {
  return request<ClinicalRecord>(`/clinical-records/${id}`, {
    method: 'PUT',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalRecordStatus(id: string, status: ClinicalRecordStatus) {
  return request<ClinicalRecord>(`/clinical-records/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  });
}

function normalizePayload(payload: ClinicalRecordPayload) {
  return {
    ...payload,
    appointmentId: payload.appointmentId?.trim() || null,
    chiefComplaint: payload.chiefComplaint?.trim() || null,
    anamnesis: payload.anamnesis?.trim() || null,
    physicalExam: payload.physicalExam?.trim() || null,
    assessment: payload.assessment?.trim() || null,
    plan: payload.plan?.trim() || null,
    notes: payload.notes?.trim() || null
  };
}
