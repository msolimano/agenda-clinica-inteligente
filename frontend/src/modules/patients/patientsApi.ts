import type { Patient, PatientPayload, PatientSummary } from './patients.types';

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

export async function listPatients(params: { search?: string; includeInactive?: boolean }) {
  const query = new URLSearchParams();

  if (params.search) {
    query.set('search', params.search);
  }

  if (params.includeInactive) {
    query.set('includeInactive', 'true');
  }

  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<PatientSummary[]>(`/patients${suffix}`);
}

export async function getPatient(id: string) {
  return request<Patient>(`/patients/${id}`);
}

export async function createPatient(payload: PatientPayload) {
  return request<Patient>('/patients', {
    method: 'POST',
    body: JSON.stringify(cleanPayload(payload))
  });
}

export async function updatePatient(id: string, payload: PatientPayload) {
  return request<Patient>(`/patients/${id}`, {
    method: 'PUT',
    body: JSON.stringify(cleanPayload(payload))
  });
}

export async function updatePatientStatus(id: string, active: boolean) {
  return request<Patient>(`/patients/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ active })
  });
}

function cleanPayload(payload: PatientPayload) {
  return Object.fromEntries(
    Object.entries(payload).map(([key, value]) => [key, value === '' ? null : value])
  );
}
