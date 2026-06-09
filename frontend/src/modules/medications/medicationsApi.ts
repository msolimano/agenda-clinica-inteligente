import type { MedicationCatalog, MedicationCatalogPayload } from './medications.types';

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

export async function listMedications(params: { search?: string; organizationId?: string | null; includeInactive?: boolean } = {}) {
  const query = new URLSearchParams();
  if (params.search) {
    query.set('search', params.search);
  }
  if (params.organizationId) {
    query.set('organizationId', params.organizationId);
  }
  if (params.includeInactive) {
    query.set('includeInactive', 'true');
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<MedicationCatalog[]>(`/medications${suffix}`);
}

export async function searchMedications(queryValue: string, organizationId?: string | null) {
  const query = new URLSearchParams();
  if (queryValue) {
    query.set('q', queryValue);
  }
  if (organizationId) {
    query.set('organizationId', organizationId);
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<MedicationCatalog[]>(`/medications/search${suffix}`);
}

export async function getMedication(id: string) {
  return request<MedicationCatalog>(`/medications/${id}`);
}

export async function createMedication(payload: MedicationCatalogPayload) {
  return request<MedicationCatalog>('/medications', {
    method: 'POST',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateMedication(id: string, payload: MedicationCatalogPayload) {
  return request<MedicationCatalog>(`/medications/${id}`, {
    method: 'PUT',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateMedicationStatus(id: string, status: 'active' | 'inactive') {
  return request<MedicationCatalog>(`/medications/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  });
}

function normalizePayload(payload: MedicationCatalogPayload) {
  return {
    ...payload,
    organizationId: payload.organizationId?.trim() || null,
    medicationCode: payload.medicationCode?.trim() || null,
    medicationCodeSystem: payload.medicationCodeSystem?.trim() || null,
    medicationName: payload.medicationName.trim(),
    activeIngredient: payload.activeIngredient?.trim() || null,
    presentation: payload.presentation?.trim() || null,
    strength: payload.strength?.trim() || null,
    pharmaceuticalForm: payload.pharmaceuticalForm?.trim() || null,
    route: payload.route?.trim() || null,
    manufacturer: payload.manufacturer?.trim() || null
  };
}
