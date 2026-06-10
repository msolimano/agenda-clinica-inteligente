import { formatDateTime } from './patientPortalFormat';
import type { PatientPortalAppointment } from './patientPortal.types';

interface PatientAppointmentsPanelProps {
  appointments: PatientPortalAppointment[];
}

export function PatientAppointmentsPanel({ appointments }: PatientAppointmentsPanelProps) {
  const upcoming = appointments.filter((appointment) => appointment.upcoming).sort((a, b) => a.startAt.localeCompare(b.startAt));
  const history = appointments.filter((appointment) => !appointment.upcoming).slice(0, 8);
  return (
    <section className="patient-portal-card">
      <div className="patient-portal-card__header"><h2>Horas medicas</h2><span>Lectura</span></div>
      <AppointmentList title="Proximas horas" items={upcoming} emptyLabel="Sin proximas horas registradas." />
      <AppointmentList title="Historial de citas" items={history} emptyLabel="Sin historial de citas." />
    </section>
  );
}

function AppointmentList({ title, items, emptyLabel }: { title: string; items: PatientPortalAppointment[]; emptyLabel: string }) {
  return (
    <div className="patient-list-block">
      <h3>{title}</h3>
      {items.length ? items.map((appointment) => (
        <article className="patient-list-item" key={appointment.id}>
          <div><strong>{formatDateTime(appointment.startAt)}</strong><span>{appointment.professionalName}</span></div>
          <span className={`patient-portal-badge patient-portal-badge--${appointment.status}`}>{appointment.status}</span>
          {appointment.reason ? <p>{appointment.reason}</p> : null}
        </article>
      )) : <p className="patient-empty">{emptyLabel}</p>}
    </div>
  );
}
