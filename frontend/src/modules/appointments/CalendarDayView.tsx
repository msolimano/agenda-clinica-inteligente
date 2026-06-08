import { formatTime } from './appointmentsDate';
import { AppointmentCard } from './AppointmentCard';
import type { Appointment, AppointmentSlot } from './appointments.types';

interface CalendarDayViewProps {
  slots: AppointmentSlot[];
  appointments: Appointment[];
  onConfirm: (appointment: Appointment) => void;
  onCancel: (appointment: Appointment) => void;
  onNoShow: (appointment: Appointment) => void;
  onReschedule: (appointment: Appointment) => void;
}

export function CalendarDayView({ slots, appointments, onConfirm, onCancel, onNoShow, onReschedule }: CalendarDayViewProps) {
  return (
    <section className="agenda-day" aria-label="Vista diaria de agenda">
      {slots.map((slot) => {
        const appointment = slot.appointmentId ? appointments.find((item) => item.id === slot.appointmentId) : undefined;
        return (
          <div className={`agenda-day__slot agenda-day__slot--${slot.status}`} key={`${slot.startAt}-${slot.endAt}`}>
            <span className="agenda-day__time">{formatTime(slot.startAt)}</span>
            <div className="agenda-day__content">
              {appointment ? (
                <AppointmentCard appointment={appointment} onConfirm={onConfirm} onCancel={onCancel} onNoShow={onNoShow} onReschedule={onReschedule} />
              ) : (
                <span className="agenda-day__available">Cupo disponible</span>
              )}
            </div>
          </div>
        );
      })}
      {slots.length === 0 ? <div className="agenda-day__empty">No hay disponibilidad configurada para este día.</div> : null}
    </section>
  );
}
