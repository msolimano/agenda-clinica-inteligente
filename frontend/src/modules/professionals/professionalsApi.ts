import type { Professional, ProfessionalPayload, Specialty } from './professionals.types';

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

export async function listProfessionals(params: { search?: string; specialtyId?: string; includeInactive?: boolean }) {
  const query = new URLSearchParams();

  if (params.search) {
    query.set('search', params.search);
  }

  if (params.specialtyId) {
    query.set('specialtyId', params.specialtyId);
  }

  if (params.includeInactive) {
    query.set('includeInactive', 'true');
  }

  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<Professional[]>(`/professionals${suffix}`);
}

export async function createProfessional(payload: ProfessionalPayload) {
  return request<Professional>('/professionals', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function updateProfessional(id: string, payload: ProfessionalPayload) {
  const { specialties: _specialties, ...professionalPayload } = payload;
  return request<Professional>(`/professionals/${id}`, {
    method: 'PUT',
    body: JSON.stringify(professionalPayload)
  });
}

export async function updateProfessionalStatus(id: string, active: boolean) {
  return request<Professional>(`/professionals/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ active })
  });
}

export async function assignSpecialty(professionalId: string, specialtyId: string, primary: boolean) {
  return request<Professional>(`/professionals/${professionalId}/specialties`, {
    method: 'POST',
    body: JSON.stringify({ specialtyId, primary })
  });
}

export async function listSpecialties() {
  return request<Specialty[]>('/specialties');
}
