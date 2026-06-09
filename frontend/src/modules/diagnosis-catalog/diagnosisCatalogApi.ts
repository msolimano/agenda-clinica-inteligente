import type { DiagnosisCatalog, DiagnosisCatalogPayload } from './diagnosisCatalog.types';

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

export async function listDiagnosisCatalog(params: { search?: string; organizationId?: string | null; includeInactive?: boolean } = {}) {
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
  return request<DiagnosisCatalog[]>(`/diagnosis-catalog${suffix}`);
}

export async function searchDiagnosisCatalog(queryValue: string, organizationId?: string | null) {
  const query = new URLSearchParams();
  if (queryValue) {
    query.set('q', queryValue);
  }
  if (organizationId) {
    query.set('organizationId', organizationId);
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<DiagnosisCatalog[]>(`/diagnosis-catalog/search${suffix}`);
}

export async function getDiagnosisCatalogItem(id: string) {
  return request<DiagnosisCatalog>(`/diagnosis-catalog/${id}`);
}

export async function createDiagnosisCatalogItem(payload: DiagnosisCatalogPayload) {
  return request<DiagnosisCatalog>('/diagnosis-catalog', {
    method: 'POST',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateDiagnosisCatalogItem(id: string, payload: DiagnosisCatalogPayload) {
  return request<DiagnosisCatalog>(`/diagnosis-catalog/${id}`, {
    method: 'PUT',
    body: JSON.stringify(normalizePayload(payload))
  });
}

export async function updateDiagnosisCatalogStatus(id: string, status: 'active' | 'inactive') {
  return request<DiagnosisCatalog>(`/diagnosis-catalog/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  });
}

function normalizePayload(payload: DiagnosisCatalogPayload) {
  return {
    ...payload,
    organizationId: payload.organizationId?.trim() || null,
    diagnosisCode: payload.diagnosisCode?.trim() || null,
    codeSystem: payload.codeSystem?.trim() || null,
    diagnosisDisplay: payload.diagnosisDisplay.trim(),
    category: payload.category?.trim() || null,
    description: payload.description?.trim() || null
  };
}
