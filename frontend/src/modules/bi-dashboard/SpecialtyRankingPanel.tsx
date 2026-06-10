import { RankingTable } from './RankingTable';
import type { BISpecialtyRanking } from './biDashboard.types';

interface SpecialtyRankingPanelProps {
  items: BISpecialtyRanking[];
}

export function SpecialtyRankingPanel({ items }: SpecialtyRankingPanelProps) {
  return (
    <section className="bi-panel bi-panel--wide">
      <div className="bi-panel__header">
        <h2>Ranking por especialidad</h2>
        <span>Top 10</span>
      </div>
      <RankingTable
        items={items}
        emptyLabel="Sin especialidades con actividad en el rango."
        getKey={(item) => item.specialtyId}
        columns={[
          { header: 'Especialidad', render: (item) => item.specialtyName },
          { header: 'Citas', align: 'right', render: (item) => item.totalAppointments },
          { header: 'Completadas', align: 'right', render: (item) => item.completedAppointments },
          { header: 'Canceladas', align: 'right', render: (item) => item.cancelledAppointments },
          { header: 'No show', align: 'right', render: (item) => item.noShowAppointments },
          { header: 'Ocupacion', align: 'right', render: (item) => `${item.occupancyRate.toFixed(1)}%` }
        ]}
      />
      <p className="bi-panel__note">Ocupacion calculada de forma aproximada sobre citas operativas.</p>
    </section>
  );
}
