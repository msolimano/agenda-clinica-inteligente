export type AppointmentStatus = 'scheduled' | 'confirmed' | 'completed' | 'cancelled' | 'no_show' | 'blocked' | 'deleted';
export type AppointmentViewMode = 'day' | 'week';
export type AppointmentType = 'consultation' | 'control' | 'procedure' | 'blocked_slot';

export interface ProfessionalOption {
  id: string;
  firstName: string;
  lastName: string;
  status: string;
}

export interface PatientOption {
  id: string;
  firstName: string;
  lastName: string;
  documentType: string | null;
  documentNumber: string | null;
  status: string;
}

export interface Appointment {
  id: string;
  organizationId: string;
  professionalId: string;
  professionalName: string;
  patientId: string | null;
  patientName: string | null;
  startAt: string;
  endAt: string;
  appointmentType: AppointmentType;
  status: AppointmentStatus;
  reason: string | null;
  cancellationReason: string | null;
  rescheduledFromId: string | null;
  confirmedAt: string | null;
  cancelledAt: string | null;
  noShowAt: string | null;
  overbooking: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface AppointmentSlot {
  startAt: string;
  endAt: string;
  status: AppointmentStatus | 'available';
  appointmentId: string | null;
  patientName: string | null;
  reason: string | null;
}

export interface AppointmentPayload {
  professionalId: string;
  patientId: string;
  startAt: string;
  endAt: string;
  appointmentType: AppointmentType;
  reason?: string;
}

export interface AppointmentReschedulePayload {
  startAt: string;
  endAt: string;
  reason?: string;
}
