import type { AppointmentType, ProfessionalOption, PatientOption } from '../appointments/appointments.types';

export type { ProfessionalOption, PatientOption };

export interface AffectedAppointment {
  appointmentId: string;
  patientId: string | null;
  patientName: string | null;
  startAt: string;
  endAt: string;
  appointmentType: AppointmentType;
  status: string;
  reason: string | null;
}

export interface SuggestedSlot {
  startAt: string;
  endAt: string;
}

export interface RescheduleSuggestion {
  appointmentId: string;
  patientId: string | null;
  patientName: string | null;
  suggestedSlots: SuggestedSlot[];
}

export interface AgendaBlockPreviewPayload {
  professionalId: string;
  startAt: string;
  endAt: string;
  reason: string;
}

export interface AgendaBlockCreatePayload extends AgendaBlockPreviewPayload {
  confirmAffectedAppointments: boolean;
}

export interface AgendaBlockPreview {
  professionalId: string;
  startAt: string;
  endAt: string;
  reason: string;
  requiresConfirmation: boolean;
  affectedAppointments: AffectedAppointment[];
  rescheduleSuggestions: RescheduleSuggestion[];
}

export interface AgendaBlock {
  id: string;
  organizationId: string;
  professionalId: string;
  professionalName: string;
  startAt: string;
  endAt: string;
  appointmentType: 'blocked_slot';
  status: string;
  reason: string;
  cancellationReason: string | null;
  cancelledAt: string | null;
  createdAt: string;
  updatedAt: string;
  affectedAppointments: AffectedAppointment[];
  rescheduleSuggestions: RescheduleSuggestion[];
}

export interface OverbookingCapacity {
  professionalId: string;
  startAt: string;
  endAt: string;
  insideAvailability: boolean;
  allowsOverbooking: boolean;
  maxOverbookings: number;
  activeOverbookings: number;
  remainingOverbookings: number;
  blocked: boolean;
  message: string;
}

export interface OverbookingPayload {
  professionalId: string;
  patientId: string;
  startAt: string;
  endAt: string;
  appointmentType: AppointmentType;
  reason: string;
}

export interface Overbooking {
  id: string;
  organizationId: string;
  appointmentId: string;
  professionalId: string;
  professionalName: string;
  patientId: string;
  patientName: string;
  startAt: string;
  endAt: string;
  appointmentType: AppointmentType;
  appointmentStatus: string;
  reason: string;
  status: string;
  createdAt: string;
  updatedAt: string;
}
