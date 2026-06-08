import type {
  AgendaBlock,
  AgendaBlockCreatePayload,
  AgendaBlockPreview,
  AgendaBlockPreviewPayload,
  Overbooking,
  OverbookingCapacity,
  OverbookingPayload,
  PatientOption,
  ProfessionalOption
} from './agendaControls.types';

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

export async function listProfessionals() {
  return request<ProfessionalOption[]>('/professionals');
}

export async function listPatients(search?: string) {
  const query = new URLSearchParams();
  if (search) {
    query.set('search', search);
  }
  const suffix = query.toString() ? `?${query.toString()}` : '';
  return request<PatientOption[]>(`/patients${suffix}`);
}

export async function previewAgendaBlock(payload: AgendaBlockPreviewPayload) {
  return request<AgendaBlockPreview>('/appointments/blocks/preview', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function createAgendaBlock(payload: AgendaBlockCreatePayload) {
  return request<AgendaBlock>('/appointments/blocks', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function listAgendaBlocks(professionalId: string, from: Date, to: Date) {
  const query = new URLSearchParams({
    professionalId,
    from: from.toISOString(),
    to: to.toISOString()
  });
  return request<AgendaBlock[]>(`/appointments/blocks?${query.toString()}`);
}

export async function cancelAgendaBlock(id: string, cancellationReason?: string) {
  return request<AgendaBlock>(`/appointments/blocks/${id}/cancel`, {
    method: 'PATCH',
    body: JSON.stringify({ cancellationReason })
  });
}

export async function getOverbookingCapacity(professionalId: string, startAt: string, endAt: string) {
  const query = new URLSearchParams({ professionalId, startAt, endAt });
  return request<OverbookingCapacity>(`/appointments/overbooking-capacity?${query.toString()}`);
}

export async function createOverbooking(payload: OverbookingPayload) {
  return request<Overbooking>('/appointments/overbookings', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function listOverbookings(professionalId: string, from: Date, to: Date) {
  const query = new URLSearchParams({
    professionalId,
    from: from.toISOString(),
    to: to.toISOString()
  });
  return request<Overbooking[]>(`/appointments/overbookings?${query.toString()}`);
}
