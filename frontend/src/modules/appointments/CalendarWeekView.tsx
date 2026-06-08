import { addDays, formatShortDate, sameLocalDate } from './appointmentsDate';
import { AppointmentCard } from './AppointmentCard';
import type { Appointment } from './appointments.types';

interface CalendarWeekViewProps {
  weekStart: Date;
  appointments: Appointment[];
  onConfirm: (appointment: Appointment) => void;
  onCancel: (appointment: Appointment) => void;
  onNoShow: (appointment: Appointment) => void;
  onReschedule: (appointment: Appointment) => void;
}

export function CalendarWeekView({ weekStart, appointments, onConfirm, onCancel, onNoShow, onReschedule }: CalendarWeekViewProps) {
  const days = Array.from({ length: 7 }, (_, index) => addDays(weekStart, index));

  return (
    <section className="agenda-week" aria-label="Vista semanal de agenda">
      {days.map((day) => {
        const dayAppointments = appointments.filter((appointment) => sameLocalDate(appointment.startAt, day));
        return (
          <div className="agenda-week__day" key={day.toISOString()}>
            <h2 className="agenda-week__title">{formatShortDate(day)}</h2>
            <div className="agenda-week__items">
              {dayAppointments.map((appointment) => (
                <AppointmentCard
                  appointment={appointment}
                  compact
                  key={appointment.id}
                  onConfirm={onConfirm}
                  onCancel={onCancel}
                  onNoShow={onNoShow}
                  onReschedule={onReschedule}
                />
              ))}
              {dayAppointments.length === 0 ? <span className="agenda-week__empty">Sin citas</span> : null}
            </div>
          </div>
        );
      })}
    </section>
  );
}
