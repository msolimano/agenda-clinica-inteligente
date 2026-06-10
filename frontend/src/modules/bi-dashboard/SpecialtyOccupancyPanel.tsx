import { SimpleHorizontalBarChart } from './SimpleHorizontalBarChart';
import type { BISpecialtyOccupancy } from './biDashboard.types';

interface SpecialtyOccupancyPanelProps {
  items: BISpecialtyOccupancy[];
}

export function SpecialtyOccupancyPanel({ items }: SpecialtyOccupancyPanelProps) {
  const chartItems = items.map((item) => ({
    label: item.specialtyName,
    value: item.occupancyRate,
    detail: `${item.occupiedAppointments}/${item.operativeAppointments} citas operativas`
  }));

  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>Ocupacion por especialidad</h2>
        <span>Aproximada</span>
      </div>
      <SimpleHorizontalBarChart
        items={chartItems}
        emptyLabel="Sin ocupacion por especialidad en el rango."
        valueFormatter={(value) => `${value.toFixed(1)}%`}
      />
      <p className="bi-panel__note">Calculo aproximado basado en citas operativas registradas.</p>
    </section>
  );
}
