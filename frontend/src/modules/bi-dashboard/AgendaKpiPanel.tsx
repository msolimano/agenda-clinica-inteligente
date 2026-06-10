import type { BIAgendaKpi, BIPatientKpi, BIWaitingListKpi } from './biDashboard.types';

interface AgendaKpiPanelProps {
  agenda: BIAgendaKpi;
  waitingList: BIWaitingListKpi;
  patients: BIPatientKpi;
}

export function AgendaKpiPanel({ agenda, waitingList, patients }: AgendaKpiPanelProps) {
  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>Agenda y admision</h2>
        <span>Ocupacion aproximada {agenda.occupancyRate}%</span>
      </div>
      <div className="bi-metric-grid">
        <Metric label="Programadas" value={agenda.scheduledAppointments} />
        <Metric label="Confirmadas" value={agenda.confirmedAppointments} />
        <Metric label="Canceladas" value={agenda.cancelledAppointments} />
        <Metric label="No show" value={agenda.noShowAppointments} />
        <Metric label="Bloqueos" value={agenda.blockedSlots} />
        <Metric label="Sobrecupos" value={agenda.overbookings} />
        <Metric label="Lista espera" value={waitingList.waitingListTotal} />
        <Metric label="Agendados desde espera" value={waitingList.waitingListScheduled} />
        <Metric label="Contactados" value={waitingList.waitingListContacted} />
        <Metric label="Cancelados espera" value={waitingList.waitingListCancelled} />
        <Metric label="Pacientes totales" value={patients.totalPatients} />
        <Metric label="Nuevos pacientes" value={patients.newPatientsInRange} />
      </div>
    </section>
  );
}

function Metric({ label, value }: { label: string; value: number }) {
  return <div className="bi-metric"><span>{label}</span><strong>{value}</strong></div>;
}
