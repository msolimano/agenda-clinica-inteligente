import { SimpleHorizontalBarChart } from './SimpleHorizontalBarChart';
import type { BIBarItem } from './biDashboard.types';

interface TopMedicationsChartPanelProps {
  items: BIBarItem[];
}

export function TopMedicationsChartPanel({ items }: TopMedicationsChartPanelProps) {
  return (
    <section className="bi-panel">
      <div className="bi-panel__header">
        <h2>Top medicamentos</h2>
        <span>Agregado</span>
      </div>
      <SimpleHorizontalBarChart items={items} emptyLabel="Sin medicamentos en el rango." />
    </section>
  );
}
