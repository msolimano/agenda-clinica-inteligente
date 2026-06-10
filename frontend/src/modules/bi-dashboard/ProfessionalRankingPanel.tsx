import { RankingTable } from './RankingTable';
import type { BIProfessionalRanking } from './biDashboard.types';

interface ProfessionalRankingPanelProps {
  items: BIProfessionalRanking[];
}

export function ProfessionalRankingPanel({ items }: ProfessionalRankingPanelProps) {
  return (
    <section className="bi-panel bi-panel--wide">
      <div className="bi-panel__header">
        <h2>Ranking por profesional</h2>
        <span>Sin datos de pacientes</span>
      </div>
      <RankingTable
        items={items}
        emptyLabel="Sin profesionales con actividad en el rango."
        getKey={(item) => item.professionalId}
        columns={[
          { header: 'Profesional', render: (item) => <span className="bi-ranking-table__primary">{item.professionalName}</span> },
          { header: 'Especialidad', render: (item) => item.specialtyName },
          { header: 'Citas', align: 'right', render: (item) => item.totalAppointments },
          { header: 'Canceladas', align: 'right', render: (item) => item.cancelledAppointments },
          { header: 'No show', align: 'right', render: (item) => item.noShowAppointments },
          { header: 'Sobrecupos', align: 'right', render: (item) => item.overbookings },
          { header: 'Fichas', align: 'right', render: (item) => item.clinicalRecordsTotal }
        ]}
      />
    </section>
  );
}
