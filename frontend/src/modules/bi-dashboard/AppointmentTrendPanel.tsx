import { SimpleTrendChart } from './SimpleTrendChart';
import type { BIAppointmentTrend } from './biDashboard.types';

interface AppointmentTrendPanelProps {
  trend: BIAppointmentTrend | null;
}

const SERIES = [
  { key: 'scheduled' as const, label: 'Agendadas', color: '#1E4E8C' },
  { key: 'confirmed' as const, label: 'Confirmadas', color: '#00B7B3' },
  { key: 'cancelled' as const, label: 'Canceladas', color: '#B42318' },
  { key: 'noShow' as const, label: 'No show', color: '#B07600' }
];

export function AppointmentTrendPanel({ trend }: AppointmentTrendPanelProps) {
  return (
    <section className="bi-panel bi-panel--wide">
      <div className="bi-panel__header">
        <h2>Tendencia diaria de citas</h2>
        <span>Estados operativos</span>
      </div>
      <SimpleTrendChart points={trend?.points ?? []} series={SERIES} emptyLabel="Sin citas en el rango seleccionado." />
    </section>
  );
}
