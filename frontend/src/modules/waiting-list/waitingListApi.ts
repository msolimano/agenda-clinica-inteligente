import type {
  PatientOption,
  ProfessionalOption,
  ScheduleFromWaitingListPayload,
  SpecialtyOption,
  WaitingListEntry,
  WaitingListPayload,
  WaitingListRecommendation
} from './waitingList.types';

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

export async function listSpecialties() {
  return request<SpecialtyOption[]>('/specialties');
}

export async function listWaitingList(params: { specialtyId?: string; professionalId?: string; status?: string }) {
  const query = new URLSearchParams();
  if (params.specialtyId) query.set('specialtyId', params.specialtyId);
  if (params.professionalId) query.set('professionalId', params.professionalId);
  if (params.status) query.set('status', params.status);
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<WaitingListEntry[]>(`/waiting-list${suffix}`);
}

export async function createWaitingListEntry(payload: WaitingListPayload) {
  return request<WaitingListEntry>('/waiting-list', {
    method: 'POST',
    body: JSON.stringify(cleanPayload(payload))
  });
}

export async function updateWaitingListEntry(id: string, payload: WaitingListPayload) {
  return request<WaitingListEntry>(`/waiting-list/${id}`, {
    method: 'PUT',
    body: JSON.stringify(cleanPayload(payload))
  });
}

export async function updateWaitingListStatus(id: string, status: string) {
  return request<WaitingListEntry>(`/waiting-list/${id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  });
}

export async function listRecommendations(params: { professionalId?: string; specialtyId?: string; startAt: string; endAt: string }) {
  const query = new URLSearchParams({ startAt: params.startAt, endAt: params.endAt });
  if (params.professionalId) query.set('professionalId', params.professionalId);
  if (params.specialtyId) query.set('specialtyId', params.specialtyId);
  return request<WaitingListRecommendation[]>(`/waiting-list/recommendations?${query.toString()}`);
}

export async function scheduleFromWaitingList(id: string, payload: ScheduleFromWaitingListPayload) {
  return request<WaitingListEntry>(`/waiting-list/${id}/schedule`, {
    method: 'POST',
    body: JSON.stringify(cleanPayload(payload))
  });
}

function cleanPayload(payload: object) {
  return Object.fromEntries(
    Object.entries(payload).map(([key, value]) => [key, value === '' ? null : value])
  );
}
