import type { ClinicalEvolution, ClinicalEvolutionPayload, ClinicalEvolutionStatus } from './clinicalEvolutions.types';

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

export async function listClinicalRecordEvolutions(recordId: string) {
  return request<ClinicalEvolution[]>(`/clinical-records/${recordId}/evolutions`);
}

export async function getClinicalEvolution(id: string) {
  return request<ClinicalEvolution>(`/clinical-evolutions/${id}`);
}

export async function createClinicalEvolution(recordId: string, payload: ClinicalEvolutionPayload) {
  return request<ClinicalEvolution>(`/clinical-records/${recordId}/evolutions`, {
    method: 'POST',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalEvolution(id: string, payload: ClinicalEvolutionPayload) {
  return request<ClinicalEvolution>(`/clinical-evolutions/${id}`, {
    method: 'PUT',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateClinicalEvolutionStatus(id: string, evolutionStatus: ClinicalEvolutionStatus) {
  return request<ClinicalEvolution>(`/clinical-evolutions/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ evolutionStatus })
  });
}

export async function listPatientEvolutions(patientId: string) {
  return request<ClinicalEvolution[]>(`/patients/${patientId}/evolutions`);
}

function normalizePayload(payload: ClinicalEvolutionPayload) {
  return {
    ...payload,
    subjective: payload.subjective?.trim() || null,
    objective: payload.objective?.trim() || null,
    assessment: payload.assessment?.trim() || null,
    plan: payload.plan?.trim() || null,
    notes: payload.notes?.trim() || null
  };
}
