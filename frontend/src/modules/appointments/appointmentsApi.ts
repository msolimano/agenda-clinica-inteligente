import type {
  Appointment,
  AppointmentPayload,
  AppointmentReschedulePayload,
  AppointmentSlot,
  PatientOption,
  ProfessionalOption
} from './appointments.types';

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

export async function listCalendar(professionalId: string, from: Date, to: Date) {
  const query = new URLSearchParams({
    professionalId,
    from: from.toISOString(),
    to: to.toISOString()
  });
  return request<Appointment[]>(`/appointments/calendar?${query.toString()}`);
}

export async function listSlots(professionalId: string, date: string) {
  const query = new URLSearchParams({ professionalId, date });
  return request<AppointmentSlot[]>(`/appointments/slots?${query.toString()}`);
}

export async function getAppointment(id: string) {
  return request<Appointment>(`/appointments/${id}`);
}

export async function createAppointment(payload: AppointmentPayload) {
  return request<Appointment>('/appointments', {
    method: 'POST',
    body: JSON.stringify(payload)
  });
}

export async function rescheduleAppointment(id: string, payload: AppointmentReschedulePayload) {
  return request<Appointment>(`/appointments/${id}/reschedule`, {
    method: 'PUT',
    body: JSON.stringify(payload)
  });
}

export async function cancelAppointment(id: string, cancellationReason?: string) {
  return request<Appointment>(`/appointments/${id}/cancel`, {
    method: 'PATCH',
    body: JSON.stringify({ cancellationReason })
  });
}

export async function confirmAppointment(id: string) {
  return request<Appointment>(`/appointments/${id}/confirm`, { method: 'PATCH' });
}

export async function registerNoShow(id: string) {
  return request<Appointment>(`/appointments/${id}/no-show`, { method: 'PATCH' });
}
