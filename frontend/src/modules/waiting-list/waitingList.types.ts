export type WaitingListStatus = 'waiting' | 'contacted' | 'scheduled' | 'cancelled' | 'deleted';
export type AppointmentType = 'consultation' | 'control' | 'procedure';

export interface PatientOption {
  id: string;
  firstName: string;
  lastName: string;
  documentType: string | null;
  documentNumber: string | null;
  status: string;
}

export interface ProfessionalOption {
  id: string;
  firstName: string;
  lastName: string;
  status: string;
}

export interface SpecialtyOption {
  id: string;
  organizationId: string | null;
  name: string;
  code: string | null;
  status: string;
}

export interface WaitingListEntry {
  id: string;
  organizationId: string;
  patientId: string;
  patientName: string;
  specialtyId: string;
  specialtyName: string;
  professionalId: string | null;
  professionalName: string | null;
  requestedFrom: string | null;
  requestedTo: string | null;
  priority: number;
  availabilityNotes: string | null;
  status: WaitingListStatus;
  scheduledAppointmentId: string | null;
  contactedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface WaitingListPayload {
  patientId: string;
  specialtyId: string;
  professionalId?: string | null;
  requestedFrom?: string | null;
  requestedTo?: string | null;
  priority: number;
  availabilityNotes?: string | null;
}

export interface WaitingListRecommendation {
  waitingListId: string;
  patientId: string;
  patientName: string;
  specialtyId: string;
  specialtyName: string;
  professionalId: string | null;
  professionalName: string | null;
  requestedFrom: string | null;
  requestedTo: string | null;
  priority: number;
  status: WaitingListStatus;
  score: number;
  reason: string;
}

export interface ScheduleFromWaitingListPayload {
  professionalId?: string | null;
  startAt: string;
  endAt: string;
  appointmentType: AppointmentType;
  reason?: string;
}
