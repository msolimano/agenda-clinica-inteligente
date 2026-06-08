import { Check, Clock, RefreshCcw, UserX, X } from 'lucide-react';
import { formatTime } from './appointmentsDate';
import type { Appointment } from './appointments.types';

interface AppointmentCardProps {
  appointment: Appointment;
  compact?: boolean;
  onConfirm: (appointment: Appointment) => void;
  onCancel: (appointment: Appointment) => void;
  onNoShow: (appointment: Appointment) => void;
  onReschedule: (appointment: Appointment) => void;
}

const statusLabels: Record<string, string> = {
  scheduled: 'Programada',
  confirmed: 'Confirmada',
  completed: 'Atendida',
  cancelled: 'Cancelada',
  no_show: 'No asistió',
  blocked: 'Bloqueada'
};

export function AppointmentCard({ appointment, compact = false, onConfirm, onCancel, onNoShow, onReschedule }: AppointmentCardProps) {
  const canAct = appointment.status === 'scheduled' || appointment.status === 'confirmed';
  const canConfirm = appointment.status === 'scheduled';

  return (
    <article className={`appointment-card appointment-card--${appointment.status}${compact ? ' appointment-card--compact' : ''}`}>
      <div className="appointment-card__main">
        <span className="appointment-card__time"><Clock aria-hidden="true" size={15} /> {formatTime(appointment.startAt)} - {formatTime(appointment.endAt)}</span>
        <strong className="appointment-card__patient">{appointment.patientName ?? 'Paciente pendiente'}</strong>
        <span className="appointment-card__reason">{appointment.reason || 'Sin motivo registrado'}</span>
      </div>
      <span className={`appointment-card__status appointment-card__status--${appointment.status}`}>{statusLabels[appointment.status] ?? appointment.status}</span>
      {!compact ? (
        <div className="appointment-card__actions">
          <button type="button" onClick={() => onReschedule(appointment)} disabled={!canAct} aria-label="Reagendar cita"><RefreshCcw aria-hidden="true" size={15} /></button>
          <button type="button" onClick={() => onConfirm(appointment)} disabled={!canConfirm} aria-label="Confirmar cita"><Check aria-hidden="true" size={15} /></button>
          <button type="button" onClick={() => onNoShow(appointment)} disabled={!canAct} aria-label="Registrar inasistencia"><UserX aria-hidden="true" size={15} /></button>
          <button type="button" onClick={() => onCancel(appointment)} disabled={!canAct} aria-label="Cancelar cita"><X aria-hidden="true" size={15} /></button>
        </div>
      ) : null}
    </article>
  );
}
